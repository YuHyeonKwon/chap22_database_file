package com.javalab.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*
 * [static 전역변수]
 * JDBC 프로그래밍을 위한 요소들을 모두 멤버변수 즉, 필드 위치로 뽑아올림.
 * - 본 클래스 어디에서라도 사용가능한 전역변수가 됨.
 */
public class Database01_field {
	// [멤버 변수]
	// 1. oracle 드라이버 이름 문자열 상수
	public static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

	// 2. oracle 데이터베이스 접속 경로(url) 문자열 상수
	public static final String DB_URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";

	// 3. 데이터베이스 전송 객체
	public static Connection con = null;

	// 4. query 실행 객체
	public static PreparedStatement pstmt = null;

	// 5. select 결과 저장 객체
	public static ResultSet rs = null;

	// 6. oracle 계정(id/pwd)
	public static String oracleId = "tempdb";
	public static String oraclePwd = "1234";

	// main 메소드
	public static void main(String[] args) {

		try {
			// 1. 드라이버 로딩
			Class.forName(DRIVER_NAME);
			System.out.println("1. 드라이버 로드 성공");

			// 2. 데이터베이스 커넥션(연결)
			con = DriverManager.getConnection(DB_URL, oracleId, oraclePwd);
			System.out.println("2. 커넥션 객체 생성 성공");

			// 3. 쿼리문
			String sql = "select c.category_name, p.product_id, p.product_name, p.price, p.receipt_date";
			sql += " from category c left outer join product p on c.category_id = p.category_id";
			sql += " order by c.category_id, p.product_id desc";

			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				System.out.println(rs.getString("category_name") + "\t" + rs.getInt("product_id") + "\t"
						+ rs.getString("product_name") + "\t" + rs.getInt("price") + "\t" + rs.getDate("receipt_date"));
			}

			// 5. Statement 객체의 executeQuery() 메소드를 통해서 쿼리 실행
			// 데이터 베이스에서 조회된 결과가 ResultSet 객체에 담겨옴
		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 ERR!: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("SQL ERR!: " + e.getMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				System.out.println("자원반납 ERR!: " + e.getMessage());
			}
		}
	} // main e

} // class e
