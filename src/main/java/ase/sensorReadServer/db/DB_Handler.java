package ase.sensorReadServer.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.sqlite.JDBC;
import org.sqlite.SQLiteConfig;

import ase.console.LogWriter;
import ase.fileIO.FileHandler;
import ase.sensorReadServer.ServerCore;
import ase.util.tableBuilder.Row;
import ase.util.tableBuilder.StringTableBuilder;

/**
 * @FileName : DB_Handler.java
 * @Project : Project2018Servers
 * @Date : 2018. 9. 23.
 * @작성자 : dja12123
 * @변경이력 :
 * @프로그램 설명 :
 */
public class DB_Handler
{
	public static final String PROP_DB_FILE = "databaseFile";

	public static final Logger databaseLogger = LogWriter.createLogger(DB_Handler.class, "db");

	private Connection connection;
	private SQLiteConfig config;
	private boolean isOpened = false;
	static
	{// test22
		try
		{
			// test11
			Class.forName("org.sqlite.JDBC");
		}
		catch (Exception e)
		{
			databaseLogger.log(Level.SEVERE, "JDBC 로드 실패", e);
		}
	}
	
	private Statement statement;

	public DB_Handler()
	{
		this.config = new SQLiteConfig();
	}

	// 실행'만' 하는 쿼리(테이블 생성, 컬럼 삭제 등)
	public boolean executeQuery(String query)
	{
		if (!this.isOpened)
			return false;
		try
		{
			return statement.execute(query);
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "질의 실패(" + query + ")", e);
			return false;
		}
	}
	
	// 결과가 나오는 쿼리 (select문)
	public CachedRowSet query(String query)
	{
		if (!this.isOpened)
		{
			databaseLogger.log(Level.SEVERE, "세션 닫힘");
			return null;
		}
		CachedRowSet crs = null;
		ResultSet rs = null;

		try
		{
			rs = this.statement.executeQuery(query);
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "질의 실패(" + query + ")", e);
			return null;
		}
		try
		{
			crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(rs);
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "CachedRowSet 만들기 실패", e);
		}

		return crs;
	}
	
	public boolean hasResult(String selectQuery)
	{
		if(!this.isOpened) return false;
		
		CachedRowSet rs = this.query(selectQuery);
		try
		{
			return rs.next();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "확인 실패", e);
		}
		return false;
	}
	
	public boolean startModule()
	{
		if (this.isOpened) return true;
		
		File f = FileHandler.getExtResourceFile(ServerCore.getProp(PROP_DB_FILE));
		databaseLogger.log(Level.INFO, "데이터베이스 열기 (" + f.toString() + ")");
		try
		{
			this.connection = DriverManager.getConnection(JDBC.PREFIX + f.toString(), this.config.toProperties());
			this.connection.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "데이터베이스 열기 실패", e);
			return false;
		}
        try
		{
			this.statement = this.connection.createStatement();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "데이터베이스 열기 실패", e);
		}
		this.isOpened = true;
        
		return true;
	}

	public void stopModule()
	{
		if (!this.isOpened)
			return;
		
		try
		{
			if(this.statement != null) this.statement.close();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "데이터베이스 닫기 실패", e);
		}

		try
		{
			if(this.connection != null) this.connection.close();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.SEVERE, "데이터베이스 닫기 실패", e);
		}
		this.isOpened = false;
		databaseLogger.log(Level.INFO, "DB_Handler 종료");
	}

	public static void printResultSet(CachedRowSet rs)
	{// https://gist.github.com/jimjam88/8559599
		databaseLogger.log(Level.INFO, "-- ResultSet INFO --");
		StringTableBuilder tb = new StringTableBuilder("No", "");
		try
		{
			if (!rs.isBeforeFirst())
				rs.beforeFirst();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();

			for (int i = 1; i <= columnsNumber; ++i)
			{
				tb.addHeadData(rsmd.getColumnName(i));
			}

			for (int i = 1; rs.next(); ++i)
			{
				Row r = tb.addRow(String.valueOf(i));
				for (int j = 1; j <= columnsNumber; ++j)
				{
					r.put(rs.getString(j));
				}
			}
			rs.beforeFirst();
		}
		catch (Exception e)
		{
			databaseLogger.log(Level.WARNING, "프린트 오류", e);
		}
		System.out.println(tb.build());
	}

	public static boolean isExist(CachedRowSet rs, String key, int col)
	{
		try
		{
			if (!rs.isBeforeFirst())
				rs.beforeFirst();
			int columnsNumber = rs.getMetaData().getColumnCount();
			if (!(col > 0 && col <= columnsNumber))
			{
				databaseLogger.log(Level.WARNING, "검색 column no 오류(1~" + columnsNumber + " input:" + col);
				return false;
			}
			while (rs.next())
			{
				if (rs.getString(col).equals(key))
				{
					rs.beforeFirst();
					return true;
				}
			}
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.WARNING, "검색 오류", e);
		}
		return false;
	}

	public static String[][] toArray(CachedRowSet rs)
	{
		LinkedList<String[]> list = null;
		try
		{
			if (!rs.isBeforeFirst())
				rs.beforeFirst();
			list = new LinkedList<String[]>();
			int columnsNumber = rs.getMetaData().getColumnCount();

			while (rs.next())
			{
				String[] rowArr = new String[columnsNumber];
				for (int i = 1; i <= columnsNumber; ++i)
				{
					rowArr[i - 1] = rs.getString(i);
				}
				list.add(rowArr);
			}
			rs.beforeFirst();
		}
		catch (SQLException e)
		{
			databaseLogger.log(Level.WARNING, "toArray 오류", e);
			return null;
		}
		String[][] arr = new String[list.size()][];
		list.toArray(arr);
		return arr;
	}
}