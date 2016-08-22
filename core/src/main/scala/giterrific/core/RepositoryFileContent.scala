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
