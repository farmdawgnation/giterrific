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
