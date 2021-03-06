package controllers

import play.api.libs.json._
import controllers.Writes._

object Branches extends Application {

  def branches = IsAuthorizedComponent {
    component =>
      implicit request =>
        val branches = component.branchRepository.getBranchesWithLastBuild

        Ok(Json.toJson(branches.toList))
  }


  def activities(branchName: String) = IsAuthorizedComponent {
    component =>
      request =>
        val branch = component.branchRepository.getBranch(branchName)
        val activity = branch.map(component.branchRepository.getBranchActivities).getOrElse(Nil)
        Ok(Json.toJson(activity))
  }
}
