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
public class DatabaseMain {
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

		// 2. 전 상품의 카테고리명과 상품명, 가격, 입고일자를 출력 메소드 호출
		selectProduct();

		// 3. 카테고리가 전자제품인 상품들의 카테고리명, 상품명 가격을 조회
		String categoryName = "전자제품";
		selectProductByCategory(categoryName);

		// 4. 가격이 25,000원 이상인 상품들의 이름과 가격을 조회하시오.
		selectProductGatherThan();

		// 5. 카테고리별로 카테고리명과 가격의 합계금액을 조회하되 금액이 큰 순서로 정렬
		selectProductGroupByCategory();

		// 6. 상품 추가 :: 카테고리:식료품 / 상품ID:기존 번호+1 상품명:양배추 / 가격:2000 / 입고일:2022/07/10
		insertProduct();

		// 7. 상품 가격 변경(수정) 탱크로리 상품의 가격을 600000 으로 수정
		updateProduct();

		// 8. 자원반환
		closeResource();

		// 9. 자원반환
		closeResource( pstmt, rs);
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
			// 3. preparedStatement 객체를 통해서 쿼리하기 위한
			// SQL 쿼리문장만들기(삽입,수정,삭제,조회)
			// ...............쿼리문 구현....................
			String sql = "select c.category_id, c.category_name, p.product_id, p.product_name, p.price, to_char(p.receipt_date, 'yyyy-mm-dd') as receipt_date";
			sql += " from category c left outer join product p on c.category_id = p.category_id";
			sql += " order by c.category_id asc, p.product_id desc";
			// 4.커넥션 객체를 통해서 데이터 베이스에 쿼리(SQL)를 실행해주는 preparedStatement 객체 얻음
			pstmt = con.prepareStatement(sql);
			System.out.println("3. pstmt 객체 생성 성공");

			// 5. Statement 객체의 excecuteQuery() 메소드를 통해서 쿼리 실행
			// 데이터 베이스에서 조회된 결과가 ResultSet 객체에 담겨옴
			rs = pstmt.executeQuery();
			System.out.println();

			// 6. rs.next()의 의미 설명
			while (rs.next()) {
				System.out.println(rs.getInt("category_id") + "\t" + rs.getString("category_name") + "\t"
						+ rs.getInt("product_id") + "\t" + rs.getString("product_name") + "\t" + rs.getInt("price")
						+ "\t" + rs.getDate("receipt_date"));
			}
		} catch (SQLException e) {
			System.out.println("SQL ERR : " + e.getMessage());
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
				System.out.println("자원반납 ERR: " + e.getMessage());
			}
		}
		/*
		 * [커넥션 객체는 계속해서 다음 메소드에서 써야 되기 때문에 닫지 않음.]
		 */
	} // end method

	// 테고리가 전자제품인 상품들의 카테고리명, 상품명 가격을 조회
	private static void selectProductByCategory(String categoryName) {
		try {
			// 1. SQL 쿼리 문장 만들기(파라미터로 전달받은 값으로 조회)
			String sql = "select c.category_id, c.category_name, p.product_id, p.product_name, p.price, to_char(p.receipt_date, 'yyyy-mm-dd') as receipt_date";
			sql += " from category c left outer join product p on c.category_id = p.category_id";
			sql += " where c.category_name = ?";
			sql += " order by c.category_id asc, p.product_id desc";
			// 2.커넥션 객체를 통해서 데이터 베이스에 쿼리(SQL)를 실행해주는 preparedStatement 객체 얻음
			pstmt = con.prepareStatement(sql);
			System.out.println();
			System.out.println("3. 카테고리가 전자제품인 상품들의 카테고리명, 상품명 가격을 조회");
			pstmt.setString(1, categoryName);

			// 3. Statement 객체의 excecuteQuery() 메소드를 통해서 쿼리 실행
			// 데이터 베이스에서 조회된 결과가 ResultSet 객체에 담겨옴
			rs = pstmt.executeQuery();

			// 4. rs.next()의 의미 설명
			while (rs.next()) {
				System.out.println(rs.getInt("category_id") + "\t" + rs.getString("category_name") + "\t"
						+ rs.getInt("product_id") + "\t" + rs.getString("product_name") + "\t" + rs.getInt("price")
						+ "\t" + rs.getDate("receipt_date"));
			}
		} catch (SQLException e) {
			System.out.println("SQL ERR : " + e.getMessage());
		} finally {
			// 자원 해제 메소드 호출
			closeResource(pstmt, rs);
			
		} // end selectAllMemner()

	} // end method

	// 가격이 25,000원 이상인 상품들의 이름과 가격 조회하는 메소드
	private static void selectProductGatherThan() {
		try {

			int wherePrice = 25000;

			String sql = "select c.category_id, c.category_name, p.product_id, p.product_name, p.price, to_char(p.receipt_date, 'yyyy-mm-dd') as receipt_date";
			sql += " from category c left outer join product p on c.category_id = p.category_id";
			sql += " where p.price >= ?";
			sql += " order by p.price desc";

			pstmt = con.prepareStatement(sql);
			System.out.println();
			System.out.println("4. 가격이 25,000원 이상인 상품들의 이름과 가격을 조회하세요.");
			pstmt.setInt(1, wherePrice);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				System.out.println(rs.getInt("category_id") + "\t" + rs.getString("category_name") + "\t"
						+ rs.getInt("product_id") + "\t" + rs.getString("product_name") + "\t" + rs.getInt("price")
						+ "\t" + rs.getDate("receipt_date"));
			}
		} catch (SQLException e) {
			System.out.println("SQL ERR : " + e.getMessage());
		} finally {
			closeResource(pstmt, rs);
		}
	} // end method

	// 카테고리별로 카테고리명과 가격의 합계금액을 조회하되 금액이 큰 순서로 정렬 메소드
	private static void selectProductGroupByCategory() {

		try {

			String sql = "select c.category_id, c.category_name, sum(p.price)";
			sql += " from category c, product p";
			sql += " where c.category_id = p.category_id";
			sql += " group by c.category_id, c.category_name";
			sql += " order by sum(price) desc";

			pstmt = con.prepareStatement(sql);
			System.out.println();
			System.out.println("5. 카테고리별로 카테고리명과 가격의 합계금액을 조회하되 금액이 큰 순서로 정렬 메소드");
			rs = pstmt.executeQuery();

			while (rs.next()) {
				System.out.println(rs.getInt("category_id") + "\t" + rs.getString("category_name") + "\t"
						+ rs.getInt("sum(p.price)") + "\t");
			}
		} catch (SQLException e) {
			System.out.println("SQL ERR : " + e.getMessage());
		} finally {
			closeResource(pstmt, rs);
		}
	} // end method

	// 상품 추가 :: 카테고리:식료품 / 상품ID:기존 번호+1 상품명:양배추 / 가격:2000 / 입고일:2022/07/10
	private static void insertProduct() {

		try {

			int productId = 22;
			String productName = "양배추";
			int price = 2000;
			int category = 5;
			String receipt = "20220710";

			String sql = "insert into product(product_id, product_name, price, category_id, receipt_date) values(?, ?, ?, ?, to_date(?,'yyyy/mm/dd'))";
			pstmt = con.prepareStatement(sql);
			System.out.println();
			System.out.println("6. 상품 추가 :: 카테고리:식료품 / 상품ID:기존 번호+1 상품명:양배추 / 가격:2000 / 입고일:2022/07/10");
			
			pstmt.setInt(1, productId);
			pstmt.setString(2, productName);
			pstmt.setInt(3, price);
			pstmt.setInt(4, category);
			pstmt.setString(5, receipt);

			int result = pstmt.executeUpdate();
			if (result > 0) {
				System.out.println("저장 성공");
			} else {
				System.out.println("저장 실패");
			}

		} catch (SQLException e) {
			System.out.println("SQL ERR : " + e.getMessage());
		} finally {
			closeResource(pstmt, rs);
		}
	} // end method

	// 상품 가격 변경(수정) 탱크로리 상품의 가격을 600000 으로 수정
	private static void updateProduct() {
		try {

			int price = 600000;
			String name = "탱크로리";

			String sql = "update product set price = ?";
			sql += " where product_name = ?";

			pstmt = con.prepareStatement(sql);
			System.out.println();
			System.out.println("7. 상품 가격 변경(수정) 탱크로리 상품의 가격을 600000 으로 수정");
			pstmt.setInt(1, price);
			pstmt.setString(2, name);

			int result = pstmt.executeUpdate();
			if (result > 0) {
				System.out.println("저장 성공");
			} else {
				System.out.println("저장 실패");
			}

		} catch (SQLException e) {
			System.out.println("SQL ERR : " + e.getMessage());
		} finally {
			closeResource(pstmt, rs);
		}
	}

	// 자원반납
	private static void closeResource() {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException e) {
			System.out.println("자원반납 ERR: " + e.getMessage());
		}
	} // end method

	// 자원반납 2
	private static void closeResource(PreparedStatement pstmt, ResultSet rs) {
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

} // class e
