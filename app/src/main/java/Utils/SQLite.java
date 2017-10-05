package Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by hai on 02/10/2017.
 */

public class SQLite extends SQLiteOpenHelper {

    public SQLite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void  queryData(String sql){
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    public Cursor getData(String sql){
        SQLiteDatabase database = getWritableDatabase();
        return database.rawQuery(sql,null);
    }

    public void insert(double lat,double lon){
        SQLiteDatabase database = getWritableDatabase();
        String command = "INSERT INTO Marker VALUES(null,null,null,null,?,?)";
        SQLiteStatement statement = database.compileStatement(command);
        statement.clearBindings();

        statement.bindDouble(1,lat);
        statement.bindDouble(2,lon);
        statement.executeInsert();
    }

    public void update(String text,byte[] image,int color,double lat,double lon){
        SQLiteDatabase database = getWritableDatabase();
        String command = "UPDATE Marker SET text=?,image=?,color=? WHERE latitude=? AND longitude=?";
        SQLiteStatement statement = database.compileStatement(command);
        statement.clearBindings();

        statement.bindString(1,text);
        statement.bindBlob(2,image);
        statement.bindLong(3,color);
        statement.bindDouble(4,lat);
        statement.bindDouble(5,lon);
        statement.executeUpdateDelete();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
