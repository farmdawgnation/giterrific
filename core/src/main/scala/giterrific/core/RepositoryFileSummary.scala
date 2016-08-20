package giterrific.core

/**
 * A data structure representing the summary of a file in a repository.
 *
 * @param sha The sha of the file's content in Git's object database.
 * @param path The path the file lives at in a working directory version of the repository.
 * @param mode The permissions on the file when it's expanded.
 * @param isDirectory True if this is a directory. False for a normal file.
 * @param size The size of the actual file.
 */
case class RepositoryFileSummary(
  sha: String,
  path: String,
  mode: String,
  isDirectory: Boolean,
  size: Long
)
