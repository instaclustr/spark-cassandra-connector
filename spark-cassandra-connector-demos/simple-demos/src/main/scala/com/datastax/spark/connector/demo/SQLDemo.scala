package com.datastax.spark.connector.demo

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.cassandra._

import com.datastax.spark.connector.cql.CassandraConnector

/** This demo creates a table in Cassandra, populates it with sample data,
  * then queries it using SparkSQL and finally displays the query results to the standard output.
  * You need to start Cassandra on local node prior to executing this demo. */
object SQLDemo extends DemoApp {

  val sqlContext = new SQLContext(sc)

  CassandraConnector(conf).withSessionDo { session =>
    session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 }")
    session.execute("DROP TABLE IF EXISTS test.sql_demo")
    session.execute("CREATE TABLE test.sql_demo (key INT PRIMARY KEY, grp INT, value DOUBLE)")
    session.execute("INSERT INTO test.sql_demo(key, grp, value) VALUES (1, 1, 1.0)")
    session.execute("INSERT INTO test.sql_demo(key, grp, value) VALUES (2, 1, 2.5)")
    session.execute("INSERT INTO test.sql_demo(key, grp, value) VALUES (3, 1, 10.0)")
    session.execute("INSERT INTO test.sql_demo(key, grp, value) VALUES (4, 2, 4.0)")
    session.execute("INSERT INTO test.sql_demo(key, grp, value) VALUES (5, 2, 2.2)")
    session.execute("INSERT INTO test.sql_demo(key, grp, value) VALUES (6, 2, 2.8)")
  }

  sqlContext.read.cassandraFormat("sql_demo", "test").load().createOrReplaceTempView("test_sql_demo")
  val rdd = sqlContext.sql("SELECT grp, max(value) AS mv FROM test_sql_demo GROUP BY grp ORDER BY mv")
  rdd.collect().foreach(println) // [2, 4.0] [1, 10.0]

  sc.stop()
}
