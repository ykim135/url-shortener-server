package irobo.api.model.dao

import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ConnectionPool, PoolConfiguration}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._

object TestDatabase {
  val pool = {
    val config            = ConfigFactory.load()
    val port              = config.getInt("port")
    val mySqlConfig       = config.getConfig("mySqlConfig")
    val dbConfig = new Configuration(
      username = mySqlConfig.getString("username"),
      host     = mySqlConfig.getString("host"),
      port     = Option(mySqlConfig.getInt("port")).getOrElse(3306),
      password = Option(mySqlConfig.getString("password")).filterNot(_.isEmpty),
      database = Some(mySqlConfig.getString("database"))
    )
    val factory = new MySQLConnectionFactory(dbConfig)

    new ConnectionPool(factory, PoolConfiguration.Default)
  }

  def disableForeignKeyCheck() = pool.sendQuery("SET FOREIGN_KEY_CHECKS=0")
  def enableForeignKeyCheck()  = pool.sendQuery("SET FOREIGN_KEY_CHECKS=1")

  def insertStockType() = pool.sendQuery("""INSERT INTO stock_type (stock_type_id, `desc`) VALUES (1, "주식"), (2, "채권")""")
  def insertCompany()      = pool.sendQuery("""INSERT INTO company (company_id, company_name) VALUES (1, "동부"), (2, "대신"), (3, "대우")""")
  def insertBranch()       = pool.sendQuery("""INSERT INTO branch (branch_id, company_id, branch_name) VALUES (1, 1, "서울"), (2, 2, "서울"), (3, 3, "서울")""")

  def insertBM() = {
    val sql = 
      s"""
      INSERT INTO bm (bm_id, bm_type, stock_ratio, bond_ratio, `desc`) 
      VALUES 
      (1, 3, 0.80, 0.20, "1"), 
      (2, 4, 0.65, 0.35, "2"), 
      (3, 5, 0.50, 0.50, "3"), 
      (4, 6, 0.40, 0.60, "4"), 
      (5, 7, 0.30, 0.70, "5"), 
      (6, 8, 0.15, 0.85, "6"),
      (7, 9, null, null, "KOSPI"),
      (8, 10, null, null, "KOBI")
      """
    
    pool.sendQuery(sql)
  }

  def insertCustomer() ={
    val sql = 
			s"""
			INSERT INTO customer (customer_id, pending_main_account_id, name, birth, address, handphone, email)
			VALUES 
			(7, 1, "ACEF01FBE7FC5FB7C4B0FF44F711BD90", "5EF895473CEC553954DDA6C63C77EBDD", "FC088C64CE1CE5EC4615EEE0882568C4", "839EE5AB9A1D3C81E70C97AC7D7F668B", "33BC691CA0C2729BF41E1F8F7A276A88FD59592EED1E0BB16D2AD852DC817D00"),
			(8, null, "D297E10BBCBA9AD1AB8588D12300F182", "1C71350843537CF72C3F1654C163BB06", "610FED8C5FED22072A70B1CB944933A1", "DC6455764979946420073AD87E278729", "F12838CD9401D3364042E4F8B735378ACAF9EEA1B22802FAF7DFEE36D716FE5B"),
			(9, null, "E34B9C6188E34233BFB29C6A738DF34B", "17FFA06BD7A85EAD13C57A0F81962130", "6DEAF003BAD652906114F9D2FDCD124F", "9E269D98A2D3A9C510D6E1E908CE789A", "6A7B2352577623ADFA42D1E4A4D7DC76")
			"""


    pool.sendQuery(sql)
  }

  def insertPB() = {
    val sql = 
      s"""
      INSERT INTO private_banker (pb_id, branch_id)
      VALUES (1, 1), (2, 2), (3, 3)
      """
    
    pool.sendQuery(sql)
  }

  def insertAccount() = {
    val sql = 
			s"""
			INSERT INTO account (customer_id, company_id, pb_id, account_name, tendency, account_num, confirm_yn, cdate)
			VALUES 
			(7, 1, 1, "ACEF01FBE7FC5FB7C4B0FF44F711BD90", 2, "DE4F450C4380174DE4FAF5679940AFF5", 1, "2017-06-03"),
			(7, 1, 1, "ACEF01FBE7FC5FB7C4B0FF44F711BD90", 3, "BC0FAACD801ADFC143ABADA441858D19", 1, "2017-05-17"),
			(8, 2, 2, "D297E10BBCBA9AD1AB8588D12300F182", 2, "17E02F79666CFC9580B5AEB978913B3B", 1, "2017-06-10"),
			(9, 3, 3, "E34B9C6188E34233BFB29C6A738DF34B", 1, "02DC3F70B9C7BEBCA9FA3780E34F1068", 1, "2017-03-03"),
			(9, 3, 3, "E34B9C6188E34233BFB29C6A738DF34B", 2, "AED7ADD3E54DAB3B91D88DEDB652F51B", 1, "2017-04-04")
			"""

    pool.sendQuery(sql)
  }

  def insertFundProduct() = {
    val sql = 
      s"""
      INSERT INTO fund_product (fund_product_id, `desc`)
      VALUES 
      (1, "일반")
      """

    pool.sendQuery(sql)
  }

  def insertFund() = {
    val sql = 
      s"""
      INSERT INTO fund (fund_code, fund_code_org, account_id, branch_id, fund_product_id, BM_id, contract_date, operation_date, close_date, fst_invest_amt, basic_fee_terms, incentive_fee_terms)
      VALUES
      (1, 1, 1, 1, 1, 4, "2017-06-03", "2017-06-05", null, 7000000, "basic1", "incentive1"),
      (2, 2, 1, 1, 1, 4, "2016-06-01", "2016-06-03", "2017-08-01", 7000000, "basic2", "incentive2"),
      (3, 3, 2, 1, 1, 5, "2017-05-17", "2017-05-18", null, 10000000, "basic3", "incentive3"),
      (4, 5, 3, 1, 1, 5, "2017-06-10", "2017-06-12", null, 42000000, "basic4", "incentive4"),
      (5, 5, 3, 1, 1, 4, "2016-06-10", "2016-06-12", null, 40000000, "basic5", "incentive5"),
      (6, 6, 4, 1, 1, 3, "2017-03-03", "2017-03-05", null, 8000000, "basic6", "incentive6"),
      (7, 7, 5, 1, 1, 1, "2017-04-04", "2017-04-06", "2017-09-01", 9000000, "basic7", "incentive7")
      """

    pool.sendQuery(sql)
  }

  def insertFundIO() = {
    val sql = 
      s"""
      INSERT INTO fund_io (fund_io_id, fund_code, in_out_type, in_out_amt, in_out_date)
      VALUES
      (1, 1, 1, 1000000, "2017-06-05"),
      (2, 1, 1, 6000000, "2017-06-03"),
      (3, 1, 0, 7200000, "2017-04-27"),
      (4, 1, 1, 7000000, "2016-06-01"),
      (5, 2, 1, 10000000, "2017-05-17"),
      (6, 3, 1, 40000000, "2016-06-10"),
      (7, 4, 1, 8000000, "2017-03-03")
      """

    pool.sendQuery(sql)
  }

  def truncate() = {
    val tables = Seq("account", "bm", "branch", "company", "customer", "fund", "fund_io", "fund_product", "private_banker")

    Future.sequence(tables.map { table =>
      for {
        _ <- disableForeignKeyCheck()
        _ <- pool.sendQuery(s"TRUNCATE TABLE $table")
      } yield ()
    })
  }

  def insert() = {
    for {
      _ <- truncate()
      _ <- insertBM()
      _ <- insertCompany()
      _ <- insertBranch()
      _ <- insertCustomer()
      _ <- insertPB()
      _ <- insertAccount()
      _ <- insertFundProduct()
      _ <- insertFund()
      _ <- insertFundIO()
      _ <- enableForeignKeyCheck()
    } yield ()
  }
}
