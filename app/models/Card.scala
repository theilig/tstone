package models

import java.sql.Connection

class Card(name: String, imageName: String) {
  def write(connection: Connection): Unit = {
    val statement = connection.createStatement()
    statement.execute(s"INSERT INTO Card (name, image) VALUES ('$name', '$imageName')")
    statement.close()
  }
}

object Card {
  def apply(line: String): Card = {
    val fields = line.split("""\s+""")
    val name = fields.tail.takeWhile(f => !f.contains(":")).mkString(" ")
    val attributes = fields.dropWhile(f => !f.contains(":")).map(f => {
      val pair = f.split(':')
      if (pair.length < 2) {
        println(s"Can't parse $line")
      }
      (pair(0), pair(1))
    }).toMap
    new Card(name, s"card${fields.head}.png")
  }
}
