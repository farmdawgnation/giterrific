package giterrific.core

import java.util.Date

/**
 * A data structure representing the identitiy of someone mentioned in commit data.
 * This is typically used to identify the author and committer.
 *
 * @param date The timestamp for when the person touched the commit.
 * @param name The name of the person.
 * @param email The email address of the person.
 */
case class RepositoryCommitIdentity(
  date: Date,
  name: String,
  email: String
)

/**
 * A data structure representing a summary of a commit in the repository.
 *
 * @param sha The sha identifier of the commit.
 * @param author The person who authored the commit.
 * @param committer The person who committed the commit into the repo.
 * @param message The message associated with the commit.
 */
case class RepositoryCommitSummary(
  sha: String,
  author: RepositoryCommitIdentity,
  committer: RepositoryCommitIdentity,
  message: String
)
