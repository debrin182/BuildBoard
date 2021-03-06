package components

import models.{User, Branch}

trait BranchServiceComponent {
  val branchService: BranchService

  trait BranchService {
    def getBranches: List[Branch]
  }

}


