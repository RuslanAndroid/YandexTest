package ru.translator.util;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ru.translator.interfaces.FavDataChangeListener;
import ru.translator.interfaces.HistoryDataChangeListener;
import ru.translator.util.items.DataBean;
//работа с бд
public class DB {
    private static HistoryDataChangeListener sHistoryDataChangeListener;

    public void setHistoryDataChangeListener(HistoryDataChangeListener historyDataChangeListener){
        sHistoryDataChangeListener =historyDataChangeListener;
    }
    public void setFavDataChangeListener(FavDataChangeListener favDataChangeListener){
        sFavDataChangeListener =favDataChangeListener;
    }
    private static FavDataChangeListener sFavDataChangeListener;
    private static final String DB_NAME = "mydb";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE = "mytab";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TXT_FROM = "textfrom";
    private static final String COLUMN_TXT_TO = "textto";

    private static final String COLUMN_LANG_FROM = "langfrom";
    private static final String COLUMN_lANG_TO = "langto";

    private static final String COLUMN_LANG_FROM_TEXT = "langfromtext";
    private static final String COLUMN_lANG_TO_TEXT = "langtotext";

    private static final String COLUMN_FAVORITE = "fav";

    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_TXT_FROM + " text ," +
                    COLUMN_TXT_TO + " text ," +
                    COLUMN_LANG_FROM + " text ," +
                    COLUMN_lANG_TO + " text ," +
                    COLUMN_LANG_FROM_TEXT + " text ," +
                    COLUMN_lANG_TO_TEXT + " text ," +
                    COLUMN_FAVORITE + " integer default 0" +
                    ");";

    private final Context mCtx;


    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public List<DataBean> getAllData() {
        List<DataBean> list = new ArrayList<>();

        Cursor cursor = mDB.query(DB_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            list.add(generateItem(cursor));
        }
        cursor.close();
        return list;
    }
    public int getCount() {
        String countQuery = "SELECT  * FROM " + DB_TABLE;
        Cursor cursor = mDB.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }
    // получить избранные записи из таблицы DB_TABLE
    public List<DataBean> getAllFavoriteData() {
        List<DataBean> list = new ArrayList<>();
        Cursor cursor = mDB.query(DB_TABLE,
                null,
                COLUMN_FAVORITE+"= ?", new String[] {String.valueOf(1)}, null, null, null);
        while (cursor.moveToNext()) {
            list.add(generateItem(cursor));
        }
        cursor.close();
        return list;
    }

    private DataBean generateItem(Cursor cursor){
        int index = cursor.getColumnIndex(COLUMN_ID);
        int index2 = cursor.getColumnIndex(COLUMN_TXT_FROM);
        int index3 = cursor.getColumnIndex(COLUMN_TXT_TO);
        int index4 = cursor.getColumnIndex(COLUMN_LANG_FROM);
        int index5 = cursor.getColumnIndex(COLUMN_lANG_TO);
        int index6 = cursor.getColumnIndex(COLUMN_LANG_FROM_TEXT);
        int index7 = cursor.getColumnIndex(COLUMN_lANG_TO_TEXT);
        int index8 = cursor.getColumnIndex(COLUMN_FAVORITE);
        int cid = cursor.getInt(index);
        String textFrom = cursor.getString(index2);
        String textTo = cursor.getString(index3);
        String langFrom = cursor.getString(index4);
        String langTo = cursor.getString(index5);
        String langFromText = cursor.getString(index6);
        String langToText = cursor.getString(index7);
        boolean fav = cursor.getInt(index8) == 1;
        return new DataBean(
                cid,
                textFrom,
                textTo,
                langFrom,
                langTo,
                langFromText,
                langToText,
                fav);
    }

    // добавить запись в DB_TABLE
    public void addRec(String txtFrom,String txtTo,String langFrom,String langTo,String langFromText,String langToText, boolean fav) {
        if(!txtFrom.equals("")&&!txtTo.equals("")){
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_TXT_FROM, txtFrom);
            cv.put(COLUMN_TXT_TO, txtTo);
            cv.put(COLUMN_LANG_FROM, langFrom);
            cv.put(COLUMN_lANG_TO, langTo);
            cv.put(COLUMN_LANG_FROM_TEXT, langFromText);
            cv.put(COLUMN_lANG_TO_TEXT, langToText);
            cv.put(COLUMN_FAVORITE, (fav)? 1 : 0);

            sHistoryDataChangeListener.onInsert(
                    new DataBean((int)mDB.insert(DB_TABLE, null, cv),
                            txtFrom,
                            txtTo,
                            langFrom,
                            langTo,
                            langFromText,
                            langToText,
                            fav));
        }
    }
    //изменить значение избранности
    public void addFavRec(int rowId,boolean newValue){
        int value =newValue? 1 : 0;

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FAVORITE,value);
        mDB.update(DB_TABLE, cv, "_id="+rowId, null);
        if(newValue){
            Cursor cursor = mDB.rawQuery("SELECT * FROM "+DB_TABLE+" WHERE _id = " + rowId, null);
            if (cursor.moveToFirst()){
                sFavDataChangeListener.onInsert(generateItem(cursor));
            }
            cursor.close();
        }else {
            sFavDataChangeListener.onFavRemove(rowId);
        }
        sHistoryDataChangeListener.onFavChange(rowId);

    }

    //удалить все записи
    public void removeAll(){
        mDB.execSQL("delete from "+ DB_TABLE);
        sHistoryDataChangeListener.onRemoveData();
        sFavDataChangeListener.onRemoveData();
    }
    //удалить избранные записи
    public void removeAllFav(){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FAVORITE,0);
        mDB.update(DB_TABLE, cv,null, null);
        sFavDataChangeListener.onRemoveData();
        sHistoryDataChangeListener.onRemoveFavData();
    }
    // удалить запись из DB_TABLE
    public void delRec(long id) {
        mDB.delete(DB_TABLE, COLUMN_ID + " = " + id, null);
    }

    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}