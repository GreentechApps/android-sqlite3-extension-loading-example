#include <stdio.h>
#include <string.h>
#include <sqlite3ext.h>
#include <assert.h>
#include <android/log.h>

#define APPNAME "MyApp"
SQLITE_EXTENSION_INIT1;
#if defined(_WIN32)
#define _USE_MATH_DEFINES
#endif /* _WIN32 */


typedef unsigned char utf8_t;

struct TextInfo {
    char *modifiedText;
    int length;
};

struct WordInfo {
    char **words;
    int total;
};


#define isunicode(c) (((c)&0xc0)==0xc0)

static int arabic_unicode[65] = {
        1552,
        1553,
        1554,
        1555,
        1556,
        1557,
        1558,
        1559,
        1560,
        1561,
        1562,
        1750,
        1751,
        1752,
        1753,
        1754,
        1755,
        1756,
        1757,
        1758,
        1759,
        1760,
        1761,
        1762,
        1763,
        1764,
        1765,
        1766,
        1767,
        1768,
        1769,
        1770,
        1771,
        1772,
        1773,
        1600,
        1611,
        1612,
        1613,
        1614,
        1615,
        1616,
        1617,
        1618,
        1619,
        1620,
        1621,
        1622,
        1623,
        1624,
        1625,
        1626,
        1627,
        1628,
        1629,
        1630,
        1631,
        1648, // 58

        1571, 1573, 1570, 1649, // 62 alif replace
        1610,
        1569,
        1607, // 65
};

char *aliff = "ا";
char *r1 = "ى";
char *r2 = "ئ";
char *r3 = "ة";

int unicode_diacritic(int code) {

    int found = 0;
    for (int i = 0; i < 64; i++) {
        if (arabic_unicode[i] == code) {
            found = i;
            break;
        }
    }

    return found;
}

int utf8_decode(const char *str, int *i) {
    const utf8_t *s = (const utf8_t *) str; // Use unsigned chars
    int u = *s, l = 1;
    if (isunicode(u)) {
        int a = (u & 0x20) ? ((u & 0x10) ? ((u & 0x08) ? ((u & 0x04) ? 6 : 5) : 4) : 3) : 2;
        if (a < 6 || !(u & 0x02)) {
            int b, p = 0;
            u = ((u << (a + 1)) & 0xff) >> (a + 1);
            for (b = 1; b < a; ++b)
                u = (u << 6) | (s[l++] & 0x3f);
        }
    }
    if (i) *i += l;
    return u;
}

struct TextInfo *remove_diacritic(const char *text, int debug) {
    if (debug) printf("\nremove_diacritic START %s\n", text);
    struct TextInfo *info = (struct TextInfo *) sqlite3_malloc(sizeof(struct TextInfo *));

    int total = 0;
    for (; text[total] != '\0'; total++);
    if (debug) printf("\nTOTAL LENGTH %d\n", total);

    int l;
    char *replaced = (char *) sqlite3_malloc(total + 5);
    int j = 0;
    for (int i = 0; text[i] != '\0';) {
        if (!isunicode(text[i])) {
            *(replaced + j++) = text[i];
            i++;
        } else {
            l = 0;
            int z = utf8_decode(&text[i], &l);
            i += l;
            int index = unicode_diacritic(z);
            if (index == 0) {
                *(replaced + j++) = text[i - 2];
                *(replaced + j++) = text[i - 1];
            } else if (index >= 58 && index <= 61) {
                *(replaced + j++) = aliff[0];
                *(replaced + j++) = aliff[1];
            } else if (index >= 62 && index <= 64) {
                if (index == 62) {
                    *(replaced + j++) = r1[0];
                    *(replaced + j++) = r1[1];
                } else if (index == 63) {
                    *(replaced + j++) = r2[0];
                    *(replaced + j++) = r2[1];
                } else if (index == 64) {
                    *(replaced + j++) = r3[0];
                    *(replaced + j++) = r3[1];
                }
            }
        }
    }

    replaced[j] = '\0';
    if (debug) printf("\n\nLENGTH: %d\n\n", j);
    info->modifiedText = replaced;
    info->length = j;
    if (debug) printf("\nremove_diacritic END %s\n", replaced);
    return info;
}


struct WordInfo *splitInWordWithLength(const char *text, int length, int debug) {

    if (debug == 1) printf("\nsplitInWordWithLength %d\n", length);
    int totalWords = 0;
    int totalChar = 0;
    for (; totalChar < length; totalChar++) {
        if (text[totalChar] == ' ') {
            totalWords += 1;
        }
    }
    totalWords += 1;
    totalChar = length;

    struct WordInfo *info = (struct WordInfo *) sqlite3_malloc(sizeof(struct WordInfo *));
    char **words = sqlite3_malloc(totalWords * sizeof(char *));
    int wordIndex = -1;
    int i = 0;
    int j = -1;
    char *word = (char *) sqlite3_malloc(totalChar + 5);
    for (; i < totalChar; i++) {
        if (debug) printf("\n%c\n", text[i]);
        if (text[i] == ' ') {
            if (j >= 0) {
                word[++j] = '\0';
                words[++wordIndex] = word;
                j = -1;
                word = (char *) sqlite3_malloc(totalChar + 5);
            }
        } else {
            word[++j] = text[i];
        }
    }

    // for last word
    if (j >= 0) {
        word[++j] = '\0';
        words[++wordIndex] = word;
    }

    info->words = words;
    info->total = wordIndex + 1;
    return info;
}

static fts5_api *fts5_api_from_db(sqlite3 *db) {
    fts5_api *pRet = 0;
    sqlite3_stmt *pStmt = 0;

    int version = sqlite3_libversion_number();
    if (version >= 3020000) {  // current api
        if (SQLITE_OK == sqlite3_prepare(db, "SELECT fts5(?1)", -1, &pStmt, 0)) {
            sqlite3_bind_pointer(pStmt, 1, (void *) &pRet, "fts5_api_ptr", NULL);
            sqlite3_step(pStmt);
        }
        sqlite3_finalize(pStmt);
    } else {  // before 3.20
        int rc = sqlite3_prepare(db, "SELECT fts5()", -1, &pStmt, 0);
        if (rc == SQLITE_OK) {
            if (SQLITE_ROW == sqlite3_step(pStmt) && sizeof(fts5_api *) == sqlite3_column_bytes(pStmt, 0)) {
                memcpy(&pRet, sqlite3_column_blob(pStmt, 0), sizeof(fts5_api *));
            }
            sqlite3_finalize(pStmt);
        }
    }
    return pRet;
}

static int v = 0;

static int fts5ArabicTokenizerCreate(
        void *pCtx,
        const char **azArg,
        int nArg,
        Fts5Tokenizer **ppOut
) {
    *ppOut = (Fts5Tokenizer *) &v;
    return SQLITE_OK;
}

static void fts5ArabicTokenizerDelete(Fts5Tokenizer *p) {
    assert(p == (Fts5Tokenizer *) &v);
}

static int fts5ArabicTokenizerTokenize(
        Fts5Tokenizer *pTokenizer,
        void *pCtx,
        int flags,
        const char *pText, int nText,
        int (*xToken)(void *, int, const char *, int, int, int)
) {

    struct WordInfo *wInfo = splitInWordWithLength(pText, nText, 0);
    for (int i = 0; i < wInfo->total; i++) {
        struct TextInfo *info = remove_diacritic(wInfo->words[i], 0);
        int rc = xToken(pCtx, 0, info->modifiedText, info->length, 0,
                        nText);
        sqlite3_free(wInfo->words[i]);
        if (rc) return rc;
    }

    sqlite3_free(wInfo->words);
    sqlite3_free(wInfo);

    return SQLITE_OK;
}

/*
** Implementation of an auxiliary function that returns the number
** of tokens in the current row (including all columns).
*/
static void column_size_imp(
        const Fts5ExtensionApi *pApi,
        Fts5Context *pFts,
        sqlite3_context *pCtx,
        int nVal,
        sqlite3_value **apVal
) {
    int rc;
    int nToken;
    int nPhrase;
    int nCol;
    nCol = pApi->xColumnCount(pFts);
    nPhrase = pApi->xPhraseCount(pFts);

    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Phrase %d", nPhrase);
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Column %d", nCol);

//    int matchinfo[];
//    int termCount = matchinfo[pOffset];
//    int colCount = matchinfo[cOffset];
//    int xOffset = cOffset + 1;
//
//    for(int column = 0; column < colCount; column++){
//        double columnScore = 0.0;
//        for(int i = 0; i < termCount; i++){
//            int currentX = xOffset + (3 * (column + i * colCount));
//            double hitCount = matchinfo[currentX];
//
//            if(hitCount > 0){
//                printf("column %d i: %d score: %f %d\n", column, i, columnScore, (2 + i * column * 3) * 4);
//                columnScore += (1 + hitCount/10);
//            }
//        }
//        score += columnScore/termCount;
//    }

//    rc = pApi->xColumnSize(pFts, -1, &nToken);
    if (nPhrase == SQLITE_OK) {
        sqlite3_result_int(pCtx, nPhrase);
    } else {
        sqlite3_result_error_code(pCtx, rc);
    }
}

typedef struct Fts5MatchinfoCtx Fts5MatchinfoCtx;

#ifndef SQLITE_AMALGAMATION
typedef unsigned int u32;
#endif

struct Fts5MatchinfoCtx {
    int nCol;                       /* Number of cols in FTS5 table */
    int nPhrase;                    /* Number of phrases in FTS5 query */
    char *zArg;                     /* nul-term'd copy of 2nd arg */
    int nRet;                       /* Number of elements in aRet[] */
    u32 *aRet;                      /* Array of 32-bit unsigned ints to return */
};


/*
** Argument f should be a flag accepted by matchinfo() (a valid character
** in the string passed as the second argument). If it is not, -1 is
** returned. Otherwise, if f is a valid matchinfo flag, the value returned
** is the number of 32-bit integers added to the output array if the
** table has nCol columns and the query nPhrase phrases.
*/
static int fts5MatchinfoFlagsize(int nCol, int nPhrase, char f) {
    int ret = -1;
    switch (f) {
        case 'p':
            ret = 1;
            break;
        case 'c':
            ret = 1;
            break;
        case 'x':
            ret = 3 * nCol * nPhrase;
            break;
    }
    return ret;
}

static int fts5MatchinfoIter(
        const Fts5ExtensionApi *pApi,   /* API offered by current FTS version */
        Fts5Context *pFts,              /* First arg to pass to pApi functions */
        Fts5MatchinfoCtx *p,
        int(*x)(const Fts5ExtensionApi *, Fts5Context *, Fts5MatchinfoCtx *, char, u32 *)
) {
    int i;
    int n = 0;
    int rc = SQLITE_OK;
    char f;
    for (i = 0; (f = p->zArg[i]); i++) {
        rc = x(pApi, pFts, p, f, &p->aRet[n]);

        if (rc != SQLITE_OK) break;
        n += fts5MatchinfoFlagsize(p->nCol, p->nPhrase, f);
    }
    return rc;
}

static int fts5MatchinfoGlobalCb(
        const Fts5ExtensionApi *pApi,
        Fts5Context *pFts,
        Fts5MatchinfoCtx *p,
        char f,
        u32 *aOut
) {
    int rc = SQLITE_OK;
    switch (f) {
        case 'p':
            aOut[0] = p->nPhrase;
            break;
        case 'c':
            aOut[0] = p->nCol;
            break;
    }
    return rc;
}

static int fts5MatchinfoLocalCb(
        const Fts5ExtensionApi *pApi,
        Fts5Context *pFts,
        Fts5MatchinfoCtx *p,
        char f,
        u32 *aOut
) {
    int i;
    int rc = SQLITE_OK;

    switch (f) {
        case 'x': {
            int nMul = 3;
            int iPhrase;

            for (i = 0; i < (p->nCol * p->nPhrase); i++) aOut[i * nMul] = 0;

            for (iPhrase = 0; iPhrase < p->nPhrase; iPhrase++) {
                Fts5PhraseIter iter;
                int iOff, iCol;
                for (pApi->xPhraseFirst(pFts, iPhrase, &iter, &iCol, &iOff);
                     iOff >= 0; pApi->xPhraseNext(pFts, &iter, &iCol, &iOff)) {
                    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Phrase %d", nMul * (iCol + iPhrase * p->nCol));
                    aOut[nMul * (iCol + iPhrase * p->nCol)]++;
                }
            }

            break;
        }
    }
    return rc;
}

static Fts5MatchinfoCtx *fts5MatchinfoNew(
        const Fts5ExtensionApi *pApi,   /* API offered by current FTS version */
        Fts5Context *pFts,              /* First arg to pass to pApi functions */
        sqlite3_context *pCtx,          /* Context for returning error message */
        const char *zArg                /* Matchinfo flag string */
) {
    Fts5MatchinfoCtx *p;
    int nCol;
    int nPhrase;
    int i;
    int nInt;
    sqlite3_int64 nByte;
    int rc;

    nCol = pApi->xColumnCount(pFts);
    nPhrase = pApi->xPhraseCount(pFts);

    nInt = 0;
    for (i = 0; zArg[i]; i++) {
        int n = fts5MatchinfoFlagsize(nCol, nPhrase, zArg[i]);
        if (n < 0) {
            char *zErr = sqlite3_mprintf("unrecognized matchinfo flag: %c", zArg[i]);
            sqlite3_result_error(pCtx, zErr, -1);
            sqlite3_free(zErr);
            return 0;
        }
        nInt += n;
    }

    nByte = sizeof(Fts5MatchinfoCtx)          /* The struct itself */
            + sizeof(u32) * nInt               /* The p->aRet[] array */
            + (i + 1);                           /* The p->zArg string */
    p = (Fts5MatchinfoCtx *) sqlite3_malloc64(nByte);
    if (p == 0) {
        sqlite3_result_error_nomem(pCtx);
        return 0;
    }
    memset(p, 0, nByte);

    p->nCol = nCol;
    p->nPhrase = nPhrase;
    p->aRet = (u32 *) &p[1];
    p->nRet = nInt;
    p->zArg = (char *) &p->aRet[nInt];
    memcpy(p->zArg, zArg, i);

    rc = fts5MatchinfoIter(pApi, pFts, p, fts5MatchinfoGlobalCb);
    if (rc != SQLITE_OK) {
        sqlite3_result_error_code(pCtx, rc);
        sqlite3_free(p);
        p = 0;
    }

    return p;
}

static void fts5MatchinfoFunc(
        const Fts5ExtensionApi *pApi,   /* API offered by current FTS version */
        Fts5Context *pFts,              /* First arg to pass to pApi functions */
        sqlite3_context *pCtx,          /* Context for returning result/error */
        int nVal,                       /* Number of values in apVal[] array */
        sqlite3_value **apVal           /* Array of trailing arguments */
) {
    const char *zArg;
    Fts5MatchinfoCtx *p;
    int rc = SQLITE_OK;

    if (nVal > 0) {
        zArg = (const char *) sqlite3_value_text(apVal[0]);
    } else {
        zArg = "pcx";
    }

    p = (Fts5MatchinfoCtx *) pApi->xGetAuxdata(pFts, 0);
    if (p == 0 || sqlite3_stricmp(zArg, p->zArg)) {
        p = fts5MatchinfoNew(pApi, pFts, pCtx, zArg);
        if (p == 0) {
            rc = SQLITE_NOMEM;
        } else {
            rc = pApi->xSetAuxdata(pFts, p, sqlite3_free);
        }
    }

    if (rc == SQLITE_OK) {
        rc = fts5MatchinfoIter(pApi, pFts, p, fts5MatchinfoLocalCb);
    }
    if (rc != SQLITE_OK) {
        sqlite3_result_error_code(pCtx, rc);
    } else {
        /* No errors has occured, so return a copy of the array of integers. */
        int nByte = p->nRet * sizeof(u32);
        sqlite3_result_blob(pCtx, (void *) p->aRet, nByte, SQLITE_TRANSIENT);
    }
}

#ifdef _WIN32
__declspec(dllexport)
#endif

int sqlite3_sqlitearabictokenizer_init(sqlite3 *db, char **error, const sqlite3_api_routines *api) {
    fts5_api *ftsApi;

    fts5_tokenizer tokenizer = {fts5ArabicTokenizerCreate, fts5ArabicTokenizerDelete, fts5ArabicTokenizerTokenize};

    SQLITE_EXTENSION_INIT2(api);

    ftsApi = fts5_api_from_db(db);

    if (ftsApi) {
        ftsApi->xCreateTokenizer(ftsApi, "arabic_tokenizer", (void *) ftsApi, &tokenizer, NULL);
        ftsApi->xCreateFunction(ftsApi, "partial_map", 0, column_size_imp, 0);
        ftsApi->xCreateFunction(ftsApi, "matchinfo", 0, fts5MatchinfoFunc, 0);
        return SQLITE_OK;
    } else {
        *error = sqlite3_mprintf("Can't find fts5 extension");
        return SQLITE_ERROR;
    }
}

//int sqlite3Fts5TestRegisterMatchinfo(sqlite3 *db){
//    int rc;                         /* Return code */
//    fts5_api *pApi;                 /* FTS5 API functions */
//
//    /* Extract the FTS5 API pointer from the database handle. The
//    ** fts5_api_from_db() function above is copied verbatim from the
//    ** FTS5 documentation. Refer there for details. */
//    rc = fts5_api_from_db(db, &pApi);
//    if( rc!=SQLITE_OK ) return rc;
//
//    /* If fts5_api_from_db() returns NULL, then either FTS5 is not registered
//    ** with this database handle, or an error (OOM perhaps?) has occurred.
//    **
//    ** Also check that the fts5_api object is version 2 or newer.
//    */
//    if( pApi==0 || pApi->iVersion<2 ){
//        return SQLITE_ERROR;
//    }
//
//    /* Register the implementation of matchinfo() */
//
//    return rc;
//}