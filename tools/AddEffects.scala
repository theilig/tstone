import scala.io.Source

object AddEffects extends App {
  val lines = Source.fromResource("manifest.txt").getLines()

  def updateBattleEffect(outLine: String, str: String): Unit = {
    println(str)
    var valuesMap: Map[String,String] = Map()
    val fields = List(
      "effect", "need_type", "repeatable", "operation", "modifier_amount", "attribute_modified")

    var done = false
    while (!done) {
      valuesMap = Map()
      fields.foreach(f => {
        print(s"$f:")
        val resp = scala.io.StdIn.readLine()
        if (resp.trim != "") {
          valuesMap += (f ->  resp)
        }
      })
      print(valuesMap)
      print("Ok:")
      val resp = scala.io.StdIn.readLine()
      done = resp == "y"
      println(fields.map(f => valuesMap.getOrElse(f, "")).mkString(";"))
    }
  }
  def updateDungeonEffect(outLine: String, str: String): Unit = {
    println(str)
    var valuesMap: Map[String,String] = Map()
    val fields = List(
      "effect", "need_type", "repeatable", "operation", "modifier_amount", "attribute_modified")

    var done = false
    while (!done) {
      valuesMap = Map()
      fields.foreach(f => {
        print(s"$f:")
        val resp = scala.io.StdIn.readLine()
        if (resp.trim != "") {
          valuesMap += (f ->  resp)
        }
      })
      print(valuesMap)
      print("Ok:")
      val resp = scala.io.StdIn.readLine()
      done = resp == "y"
      println(fields.map(f => valuesMap.getOrElse(f, "")).mkString(";"))
    }
  }
  def updateVillageEffect(outLine: String, str: String): Unit = {}
  def updateBreachEffect(outLine: String, str: String): Unit = {}

  lines.foreach(line => {
    println(line)
    val fields = line.split(" ").flatMap(field => {
      val parts = field.split(":")
      if (parts.length >= 2) {
        Some(parts(0) -> parts(1))
      } else {
        None
      }
    }).toMap
//    if (fields.contains("be")) {
//      updateBattleEffect(line, fields("be"))
//    }
    if (fields.contains("de")) {
      updateDungeonEffect(line, fields("de"))
    }
    if (fields.contains("breack")) {
      updateBreachEffect(line, fields("breach"))
    }
    if (fields.contains("ve")) {
      updateVillageEffect(line, fields("ve"))
    }
  })
}
