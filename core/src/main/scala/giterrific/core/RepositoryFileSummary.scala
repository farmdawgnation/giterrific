package giterrific.core

/**
 * A data structure representing the summary of a file in a repository.
 *
 * @param name The name of the file.
 * @param path The path of the file.
 * @param mode The permissions on the file when it's expanded.
 * @param isDirectory True if this is a directory. False for a normal file.
 * @param size The size of the actual file.
 */
case class RepositoryFileSummary(
  name: String,
  path: String,
  mode: String,
  isDirectory: Boolean,
  size: Long
)
