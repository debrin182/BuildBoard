package controllers

import play.api.libs.json._
import models._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import models.PullRequestStatus

object Writes {
  implicit val artifactWrite: Writes[Artifact] = Json.writes[Artifact]
  implicit val testCaseWrite: Writes[TestCase] = Json.writes[TestCase]
  implicit val testCasePackageWrite: Writes[TestCasePackage] = Json.writes[TestCasePackage]
  implicit val buildNodeWrite: Writes[BuildNode] = Json.writes[BuildNode]
  implicit val buildInfoWrite = Json.writes[BuildInfo]
  implicit val buildWrite = Json.writes[Build]


  implicit val buildActionWrite = (
    (__ \ "name").write[String] ~
      (__ \ "pullRequestId").writeNullable[Int] ~
      (__ \ "branchId").writeNullable[String] ~
      (__ \ "cycleName").write[String]
    )(unlift(BuildAction.unapply))

  implicit val entityAssignment = Json.writes[Assignment]
  implicit val entityStateWrite = Json.writes[EntityState]
  implicit val entityWrite = Json.writes[Entity]

  implicit val statusWrites = (
    (__ \ "isMergeable").write[Boolean] ~
      (__ \ "isMerged").write[Boolean])(unlift(PullRequestStatus.unapply))

  implicit val prWrite = Json.writes[PullRequest]

  val activityEntryWrites = new Writes[ActivityEntry] {
    override def writes(o: ActivityEntry): JsValue = o match {
      case b@Build(_, _, _, _) => buildWrite.writes(b)
      case b@PullRequest(_, _, _, _, _) => prWrite.writes(b)
      case b@BuildInfo(_, _, _, _, _, _, _) => buildInfoWrite.writes(b)
    }
  }

  implicit val branchWrite =
    (
      (__ \ "name").write[String] ~
        (__ \ "url").write[String] ~
        (__ \ "pullRequest").writeNullable[PullRequest] ~
        (__ \ "entity").writeNullable[Entity] ~
        (__ \ "lastBuild").writeNullable[BuildInfo] ~
        (__ \ "activity").write(list(activityEntryWrites))
      )(unlift(Branch.unapply))
}
