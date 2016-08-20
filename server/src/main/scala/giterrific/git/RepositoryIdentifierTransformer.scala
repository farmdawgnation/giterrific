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
