import java.io.FileWriter

import models.game.Card

import scala.io.Source

object CreateImageManifest extends App {
  val cards: List[(String, Card)] = Source.fromResource("manifest.txt").getLines().map(line =>
    (line.split(" ").head, Card(line))).toList
  val output = new FileWriter("ui/src/img/cards/cards.js")
  output.write("import card000 from './card000.png'\n")
  cards.map {
    case (id, _) => output.write(s"import card$id from './card$id.png'\n")
  }
  output.write("\nconst cardImages = {\n")
  output.write("  'CardBack': card000,\n")
  cards.foreach {
    case (id, card) =>
      val name = card.getName
      output.write(s"""  '$name': card$id,\n""")
  }
  output.write("};\n\nexport default cardImages;")
  output.close()
}
