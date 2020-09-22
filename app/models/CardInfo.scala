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
                     battleEffects: Seq[BattleEffectRow],
                     breachEffects: Seq[BreachEffectRow],
                     dungeonEffects: Seq[DungeonEffectRow],
                     villageEffects: Seq[VillageEffectRow],
                     thunderstones: Seq[ThunderstoneRow]
                   )
