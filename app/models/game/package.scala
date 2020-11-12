package models

package object game {
  type Attributes = Map[String, Int]
  def applyAdjustments(
                        attributes: Attributes,
                        adjustments: List[Attributes => Attributes]): Attributes = {
    adjustments.foldLeft(attributes)((current, adjustment) => adjustment(current))
  }

  def combineAttributes(attributeGroup: Seq[Attributes]): Attributes = {
    attributeGroup.flatten.groupBy(_._1).map {
      case (key, grouped) => key -> grouped.map(_._2).sum
    }
  }
}
