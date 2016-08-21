package giterrific.core

/**
 * A data structure representing the file content of a file within the repository.
 *
 * @param name The name of the file.
 * @param content The content of the file.
 * @param encoding The encoding used for the content of the file. Either base64 or utf-8.
 * @param size The size of the file.
 */
case class RepositoryFileContent(
  name: String,
  content: String,
  encoding: String,
  size: Long
)
