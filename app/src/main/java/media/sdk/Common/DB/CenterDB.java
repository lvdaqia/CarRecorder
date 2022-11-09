package media.sdk.Common.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class CenterDB
{

	public class DatabaseHelper extends SQLiteOpenHelper
	{

		private static final String DB_FILE = "DBCenter.db";
		private static final int DB_VERSION = 1;
		private static final String SQLCMD_CREATE_TABLE_RECORD = "CREATE TABLE "
				+ TABLE_RECORD
				+ "("
				+ "ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "DeviceID VARCHAR(20),"
				+ "FileName NVARCHAR(256), "
				+ "FileType INTEGER,"
				+ "MediaType INTEGER,"
				+ "TimeStamp INTEGER,"
				+ "FileSize INTEGER"
				+ ");";
		private static final String SQLCMD_DROP_TABLE_RECORD = "DROP TABLE IF EXISTS " + TABLE_RECORD + ";";
		private static final String SQLCMD_DELETE_TABLE_RECORD = "DELETE FROM " + TABLE_RECORD + ";";

		public DatabaseHelper(Context context)
		{
			super(context, DB_FILE, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			//������ݿⲻ���ڲŻ���øú���
			db.execSQL(SQLCMD_CREATE_TABLE_RECORD);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			db.execSQL(SQLCMD_DROP_TABLE_RECORD);
			db.execSQL(SQLCMD_CREATE_TABLE_RECORD);
		}

	}

	public CenterDB(Context context)
	{
		m_DbHelper = new DatabaseHelper(context);
		m_context = context;
		SQLiteDatabase db = m_DbHelper.getReadableDatabase();
		db.close();
	}

	public static int DeleteDatabase(Context context)
	{
		context.deleteDatabase("DBCenter.db");
		return 0;
	}


	static public class DBFile
	{
		public boolean bSelected;
		public String strFileName;
		public String strDeviceID;
		public long nTimeStamp;
		public int nFileType;
		public int nMediaType;
		public int nFileSize;

		public String GetFileType()
		{
			if(nFileType == 0)
			{
				if(nMediaType == 0)
				{
					return "mp4";
				}
				return "mp4";
			}
			else if(nFileType == 1)
			{
				if(nMediaType == 0)
				{
					return "jpeg";
				}
				return "jpeg";
			}
			else if(nFileType == 2)
			{
				if(nMediaType == 0)
				{
					return "aac";
				}
				return "aac";
			}
			return "";
		}
	}

	public long AddFile(DBFile dbFile)
	{
		SQLiteDatabase db = m_DbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("DeviceID", dbFile.strDeviceID);
		values.put("FileName", dbFile.strFileName);
		values.put("FileType", dbFile.nFileType);
		values.put("MediaType", dbFile.nMediaType);
		values.put("FileSize", dbFile.nFileSize);
		values.put("TimeStamp", dbFile.nTimeStamp);
		db.insertOrThrow(TABLE_RECORD, null, values);
		db.close();
		return 0;
	}

	public int DeleteFile(String strFileName)
	{
		SQLiteDatabase db = m_DbHelper.getWritableDatabase();
		db.delete(TABLE_RECORD, "FileName = '" + strFileName + "'", null);
		db.close();
		return 0;
	}

	public int DeleteAllFile()
	{
		SQLiteDatabase db = CenterDB.Instant().m_DbHelper.getWritableDatabase();
		db.execSQL(DatabaseHelper.SQLCMD_DELETE_TABLE_RECORD);
		db.close();
		return 0;
	}

	public int LoadFileList(int nFileType, ArrayList<DBFile> lstFile)
	{
		lstFile.clear();
		String strSql = "SELECT* FROM " + CenterDB.Instant().TABLE_RECORD + " WHERE FileType = " + nFileType + " ORDER BY TimeStamp DESC" + ";";
		SQLiteDatabase db = CenterDB.Instant().m_DbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(strSql, null);
		while (cursor.moveToNext())
		{
			CenterDB.DBFile dbFile = new CenterDB.DBFile();
			long nID = cursor.getLong(0);
			dbFile.strDeviceID = cursor.getString(1);
			dbFile.strFileName = cursor.getString(2);
			dbFile.nFileType = cursor.getInt(3);
			dbFile.nMediaType = cursor.getInt(4);
			dbFile.nFileSize = cursor.getInt(5);
			dbFile.nTimeStamp = cursor.getLong(6);
			lstFile.add(dbFile);
		}
		cursor.close();
		db.close();
		return 0;
	}

	public int LoadFileList(ArrayList<DBFile> lstFile)
	{
		lstFile.clear();
		String strSql = "SELECT* FROM " + CenterDB.Instant().TABLE_RECORD + " ORDER BY TimeStamp DESC" + ";";
		SQLiteDatabase db = CenterDB.Instant().m_DbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(strSql, null);
		while (cursor.moveToNext())
		{
			CenterDB.DBFile dbFile = new CenterDB.DBFile();
			long nID = cursor.getLong(0);
			dbFile.strDeviceID = cursor.getString(1);
			dbFile.strFileName = cursor.getString(2);
			dbFile.nFileType = cursor.getInt(3);
			dbFile.nMediaType = cursor.getInt(4);
			dbFile.nFileSize = cursor.getInt(5);
			dbFile.nTimeStamp = cursor.getLong(6);
			lstFile.add(dbFile);
		}
		cursor.close();
		db.close();
		return 0;
	}

	public static CenterDB Instant()
	{
		return m_centerDB;
	}

	public static CenterDB Initial(Context ctx)
	{
		m_centerDB = new CenterDB(ctx);
		return m_centerDB;
	}

	public static CenterDB m_centerDB = null;
	public static final String TABLE_RECORD = "Record";
	DatabaseHelper m_DbHelper;
	private Context m_context = null;
}
