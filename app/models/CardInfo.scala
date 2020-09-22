package models
import schema.Tables._
case class CardInfo(
                     cardRow: CardRow,
                     heroRow: Option[HeroRow],
                     itemRow: Option[ItemRow],
                     monsterRow: Option[MonsterRow],
                     spellRow: Option[SpellRow],
                     villagerRow: Option[VillagerRow],
                     weaponRow: Option[WeaponRow],
                     turnEffects: Seq[TurnEffectRow],
                     breachEffects: Seq[BreachEffectRow],
                     thunderstones: Seq[ThunderstoneRow]
                   )
