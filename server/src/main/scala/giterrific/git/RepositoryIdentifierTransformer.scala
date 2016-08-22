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
package giterrific.git

/**
 * A utility that transforms one identifier into another. This is
 * particularly useful for converting
 */
sealed trait RepositoryIdentifierTransformer {
  def transform(input: String): String
}

/**
 * A transformer that generates a new identifier by prepending the prefix of the
 * original identifier followed by a slash.
 */
case class PrefixedIdentifier(prefixLength: Int) extends RepositoryIdentifierTransformer {
  def transform(input: String): String = {
    val prefix = input.take(prefixLength) // Magic number-ish for the moment.
    s"$prefix/$input"
  }
}

case object DotGitSuffixer extends RepositoryIdentifierTransformer {
  def transform(input: String): String = s"$input.git"
}

case class ChainedTransformer(transformers: Seq[RepositoryIdentifierTransformer]) extends RepositoryIdentifierTransformer {
  def transform(input: String): String =
    transformers.foldLeft(input)((currentId, currentTransformer) => currentTransformer.transform(currentId))
}
