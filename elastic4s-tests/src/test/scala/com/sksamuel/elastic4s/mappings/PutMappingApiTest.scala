package com.sksamuel.elastic4s.mappings

import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.mappings.dynamictemplate.DynamicMapping
import com.sksamuel.elastic4s.testkit.{DiscoveryLocalNodeProvider, DockerTests}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try

class PutMappingApiTest extends FlatSpec with Matchers with DockerTests {

  Try {
    http.execute {
      deleteIndex("index")
    }.await
  }

  http.execute {
    createIndex("index")
  }.await

  "a put mapping dsl" should "be accepted by the client" in {
    http.execute {
      putMapping("index" / "type").as(
        geopointField("name"),
        dateField("content") nullValue "no content"
      )
    }.await
  }

  it should "accept same fields as mapping api" in {
    http.execute {
      putMapping("index" / "type").as(
        dateField("content") nullValue "no content"
      ) dynamic DynamicMapping.False numericDetection true boostNullValue 12.2
    }.await
  }

  it should "accept same several new fields with different types as mapping api and return the right mapping in get" in {
    http.execute {
      putMapping("index" / "type").as(
        dateField("content") nullValue "no content",
        textField("description") boost 1.5,
        doubleField("price"),
        nestedField("children") fields(
          textField("name"),
          dateField("date") nullValue "no date"
        )
      ) dynamic DynamicMapping.False numericDetection true boostNullValue 12.2
    }.await

    //    // Using only TCP client to get mapping as the Http client doesn't have this method yet
    //    val mapping = http.execute {
    //      getMapping("index/type")
    //    }.await.mappingFor(IndexAndType("index", "type")).sourceAsMap()
    //
    //    mapping.get("dynamic") shouldBe "false"
    //    mapping.get("numeric_detection") shouldBe true
    //
    //    val props = mapping.get("properties").asInstanceOf[java.util.LinkedHashMap[String, Object]]
    //
    //    props.get("name").asInstanceOf[util.Map[String, _]].get("type") shouldBe "geo_point"
    //    props.get("price").asInstanceOf[util.Map[String, _]].get("type") shouldBe "double"
    //
    //    val contentFieldMapping = props.get("content").asInstanceOf[util.Map[String, _]]
    //    contentFieldMapping.get("type") shouldBe "date"
    //    contentFieldMapping.get("null_value") shouldBe "no content"
    //
    //    val descriptionFieldMapping = props.get("description").asInstanceOf[util.Map[String, _]]
    //    descriptionFieldMapping.get("type") shouldBe "text"
    //    descriptionFieldMapping.get("boost") shouldBe 1.5
    //
    //    val children = props.get("children").asInstanceOf[util.Map[String, Object]].get("properties").asInstanceOf[util.Map[String, Object]]
    //
    //    val dateFieldMapping = children.get("date").asInstanceOf[util.Map[String, _]]
    //    dateFieldMapping.get("type") shouldBe "date"
    //    dateFieldMapping.get("null_value") shouldBe "no date"
    //
    //    children.get("name").asInstanceOf[util.Map[String, _]].get("type") shouldBe "text"
  }
}
