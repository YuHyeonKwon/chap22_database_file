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
 *  [모듈화]
 *  - 데이터베이스 커넥션 + PreparedStatement + 쿼리실행 작업 모듈
 *  - 실제로 쿼리를 실행하고 결과를 받아오는 부분 모듈
 *  [미션]
 *  - 전체 상품의 정보를 조회하세요.(카테고리명이 나오도록)
 */
public class Database04_module03 {
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

		// 1. 디비 접속 메소드 호출
		connectDB();

		// 2. 쿼리문 실행 메소드 호출
		// - (여기서는 커넥션 객체 자원을 반납하지 않는다.)
		selectProduct();
		
		// 3. 특정 카테고리에 소속된 상품들만 조회하는 메소드
		// - (여기서는 커넥션 객체 자원을 반납한다.)
		String categoryName = "전자제품";
		selectProductByCategory(categoryName);
		
		// 4. 자원반환
		closeResource();

	} // main e

	// 드라이버 로딩과 커넥션 객체 생성 메소드
	private static void connectDB() {
		try {
			// 1. 드라이버 로딩
			Class.forName(DRIVER_NAME);
			System.out.println("1.드라이버로드 성공");

			// 2.데이터베이스 커넥션(연결)
			con = DriverManager.getConnection(DB_URL, oracleId, oraclePwd);
			System.out.println("2.커넥션 객체 생성 성공");

		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 로드 ERR : " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("SQL ERR : " + e.getMessage());
		}
	} // end connectDB()

	// 전체 상품 조회
	private static void selectProduct() {
		try {
	         //3. preparedStatement 객체를 통해서 쿼리하기 위한
	         //SQL 쿼리문장만들기(삽입,수정,삭제,조회)
	         //...............쿼리문 구현....................
	         String sql = "select c.category_id, c.category_name, p.product_id, p.product_name, p.price, to_char(p.receipt_date, 'yyyy-mm-dd') as receipt_date";
	         sql += " from category c left outer join product p on c.category_id = p.category_id";
	         sql += " order by c.category_id asc, p.product_id desc";
	         //4.커넥션 객체를 통해서 데이터 베이스에 쿼리(SQL)를 실행해주는 preparedStatement 객체 얻음
	         pstmt = con.prepareStatement(sql);
	         System.out.println("3. pstmt 객체 생성 성공");
	         
	         //5. Statement 객체의 excecuteQuery() 메소드를 통해서 쿼리 실행
	         // 데이터 베이스에서 조회된 결과가 ResultSet 객체에 담겨옴
	         rs = pstmt.executeQuery();
	         System.out.println();
	         
	         //6. rs.next()의 의미 설명
	         while(rs.next()) {
	            System.out.println(rs.getInt("category_id")+"\t"+
	                           rs.getString("category_name")+"\t"+
	                           rs.getInt("product_id")+"\t"+
	                           rs.getString("product_name")+"\t"+
	                           rs.getInt("price")+"\t"+
	                           rs.getDate("receipt_date")
	                          );
	         }
	      }catch(SQLException e) {
	         System.out.println("SQL ERR : "+e.getMessage());
	      }finally {
	    	  try {
	    		  if (rs != null) {
	    			  rs.close();
	    		  }
	    		  if (pstmt != null) {
	    			  pstmt.close();
	    		  }
	    	  }catch (SQLException e) {
	    		  System.out.println("자원반납 ERR: " + e.getMessage());
	    	  }
	      }
		/*
		 * [커넥션 객체는 계속해서 다음 메소드에서 써야 되기 때문에 닫지 않음.]
		 */
	} // end method


	// 드라이버 이름으로 상품 조회하는 메소드
	private static void selectProductByCategory(String categoryName) {
		try {
	         //1. SQL 쿼리 문장 만들기(파라미터로 전달받은 값으로 조회)
	         String sql = "select c.category_id, c.category_name, p.product_id, p.product_name, p.price, to_char(p.receipt_date, 'yyyy-mm-dd') as receipt_date";
	         sql += " from category c left outer join product p on c.category_id = p.category_id";
	         sql += " where c.category_name = ?";
	         sql += " order by c.category_id asc, p.product_id desc";
	         //2.커넥션 객체를 통해서 데이터 베이스에 쿼리(SQL)를 실행해주는 preparedStatement 객체 얻음
	         pstmt = con.prepareStatement(sql);
	         System.out.println();
	         System.out.println("3. pstmt 객체 생성 성공");
	         pstmt.setString(1, categoryName);
	         
	         //3. Statement 객체의 excecuteQuery() 메소드를 통해서 쿼리 실행
	         // 데이터 베이스에서 조회된 결과가 ResultSet 객체에 담겨옴
	         rs = pstmt.executeQuery();
	         System.out.println();
	         
	         //4. rs.next()의 의미 설명
	         while(rs.next()) {
	            System.out.println(rs.getInt("category_id")+"\t"+
	                           rs.getString("category_name")+"\t"+
	                           rs.getInt("product_id")+"\t"+
	                           rs.getString("product_name")+"\t"+
	                           rs.getInt("price")+"\t"+
	                           rs.getDate("receipt_date")
	                          );
	         }
	      }catch(SQLException e) {
	         System.out.println("SQL ERR : "+e.getMessage());
	      }finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			}catch (SQLException e) {
				System.out.println("자원반납 ERR: " + e.getMessage());
			}
		}
		
	} // end method
	
	//
	private static void closeResource() {
		try {
			if (con != null) {
				con.close();
			}
		}catch (SQLException e) {
			System.out.println("자원반납 ERR: " + e.getMessage());
		}
	} // end method
	
} // class e
