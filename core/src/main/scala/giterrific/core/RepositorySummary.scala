package giterrific.core

/**
 * A data structure providing a brief summary of a repository.
 *
 * @param name The short name of the repository.
 * @param fullName The full path to the repository on the repository server.
 * @param description The human description of the repository
 */
case class RepositorySummary(
  name: String,
  fullName: String,
  description: String
)
