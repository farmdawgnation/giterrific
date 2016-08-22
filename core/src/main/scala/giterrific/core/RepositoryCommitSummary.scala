/**
 *
 * Copyright 2016 Matthew Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
