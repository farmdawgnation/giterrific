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
