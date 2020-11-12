package models.game

import play.api.libs.json.{Format, Json}

case class BattleSlot(card: String, equipped: List[String], destroyed: List[String], hand: Option[List[Card]]) {
  def battleAttributes(generalEffects: List[TurnEffect], monster: Option[MonsterCard], rank: Int): (String, Attributes) = {
    def filterWeapons(equipped: List[Card], weaponFound: Boolean, attributes: Attributes): List[Card] = {
      equipped match {
        case (_: WeaponCard) :: xs if weaponFound =>
          filterWeapons(xs, weaponFound = true, attributes)
        case (w: WeaponCard) :: xs if w.weight > attributes.getOrElse("Strength", 0) =>
          filterWeapons(xs, weaponFound, attributes)
        case (w: WeaponCard) :: xs =>
          w :: filterWeapons(xs, weaponFound = true, attributes)
        case x :: xs =>
          x :: filterWeapons(xs, weaponFound, attributes)
        case Nil => Nil
      }
    }
    def filteredEquipped: List[Card] = {
      baseCard match {
        case h: HeroCard =>
          val effects = generalEffects ::: monster.map(m => m.battleEffects).getOrElse(Nil) :::
            equippedCards.flatMap(c => c.getDungeonEffects)
          val strengthAttribute = effects.foldLeft(Map("Strength" -> h.strength))((a, e) => {
            e.applyIndividualAdjustment(baseCard, destroyed = false)(a)
          })
          filterWeapons(equippedCards, weaponFound = false, strengthAttribute)
        case _ => Nil
      }
    }
    val finalEquipped = filteredEquipped
    baseCard match {
      case w: WeaponCard => w.name -> Map()
      case _ =>
        val monsterEffects = monster.map(_.getBattleEffects).getOrElse(Nil)
        val allCards = (baseCard :: finalEquipped).map(c => (c, false)) :::
          destroyedCards.filter(_.getDungeonEffects.exists(
            e => e.requiredType.contains("Self"))
          ).map(c => (c, true))
        val slotAttributes = allCards.map(pair => {
          val (card, destroyed) = pair
          applyAdjustments(
            card.attributes,
            ( card.getDungeonEffects ::: generalEffects).map(e => e.applyIndividualAdjustment(card, destroyed))
          )
        })
        val slotResult = applyAdjustments(
          combineAttributes(slotAttributes),
          allCards.flatMap(_._1.getDungeonEffects).map(e =>
            e.applyMatchupAdjustment(this, monster, rank, late = false)
          )
        )
        val resultWithMonster = applyAdjustments(
          slotResult,
          monsterEffects.map(e => e.applyMatchupAdjustment(this, monster, rank, late = false))
        )
        baseCard.getName -> applyAdjustments(
          resultWithMonster,
          (generalEffects ::: allCards.flatMap(_._1.getDungeonEffects)).map(e =>
            e.applyMatchupAdjustment(this, monster, rank, late = true)
          )
        )
    }
  }

  def villageAttributes: Attributes = {
    val liveCards = baseCard :: equippedCards
    val slotAttributes = liveCards.map(c => Map("Gold" -> c.getGoldValue)) :::
      baseCard.getVillageEffects.map(e =>
      e.applyMatchupAdjustment(this, None, 0, late = false)(
        e.applyIndividualAdjustment(baseCard, destroyed.contains(baseCard.getName))(Map()))
    )
    combineAttributes(slotAttributes)
  }

  def baseCard: Card = {
      hand.get.find(_.getName == card).get
  }
  def equippedCards: List[Card] = {
    equipped.map(c => hand.get.find(_.getName == c).get)
  }
  def destroyedCards: List[Card] = {
    destroyed.map(c => hand.get.find(_.getName == c).get)
  }
}

object BattleSlot {
  implicit val battleSlotFormat: Format[BattleSlot] = Json.format
}