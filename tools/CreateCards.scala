import java.sql.DriverManager

import models.game.Card

import scala.io.Source

object CreateCards extends App {
  val connection = DriverManager.getConnection(args(0), args(1), args(2))
  val statement = connection.createStatement()
  val cardsResultSet = statement.executeQuery("SELECT card_id, name FROM Card")
  var cardNameMap: Map[String, Int] = Map()
  while (cardsResultSet.next()) {
    cardNameMap += (cardsResultSet.getString(2) -> cardsResultSet.getInt(1))
  }
  cardsResultSet.close()
  statement.close()
  val cards = Source.fromResource("manifest.txt").getLines().map(line => Card(line))
  cards.foreach(c => c.write(connection))
}
