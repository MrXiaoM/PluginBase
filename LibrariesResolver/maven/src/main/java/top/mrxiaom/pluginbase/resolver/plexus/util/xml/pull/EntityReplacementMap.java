package top.mrxiaom.pluginbase.resolver.plexus.util.xml.pull;

/*
 * Copyright The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class EntityReplacementMap
{
    final String[] entityName;

    final char[][] entityNameBuf;

    final String[] entityReplacement;

    final char[][] entityReplacementBuf;

    int entityEnd;

    final int[] entityNameHash;

    public EntityReplacementMap( String[][] replacements )
    {
        int length = replacements.length;
        entityName = new String[length];
        entityNameBuf = new char[length][];
        entityReplacement = new String[length];
        entityReplacementBuf = new char[length][];
        entityNameHash = new int[length];

        for ( String[] replacement : replacements )
        {
            defineEntityReplacementText( replacement[0], replacement[1] );
        }
    }

    private void defineEntityReplacementText( String entityName, String replacementText )
    {
        if ( !replacementText.startsWith( "&#" ) && this.entityName != null && replacementText.length() > 1 )
        {
            String tmp = replacementText.substring( 1, replacementText.length() - 1 );
            for ( int i = 0; i < this.entityName.length; i++ )
            {
                if ( this.entityName[i] != null && this.entityName[i].equals( tmp ) )
                {
                    replacementText = this.entityReplacement[i];
                }
            }
        }

        // this is to make sure that if interning works we will take advantage of it ...
        char[] entityNameCharData = entityName.toCharArray();
        // noinspection ConstantConditions
        this.entityName[entityEnd] = newString( entityNameCharData, 0, entityName.length() );
        entityNameBuf[entityEnd] = entityNameCharData;

        entityReplacement[entityEnd] = replacementText;
        entityReplacementBuf[entityEnd] = replacementText.toCharArray();
        entityNameHash[entityEnd] = fastHash( entityNameBuf[entityEnd], 0, entityNameBuf[entityEnd].length );
        ++entityEnd;
        // TODO disallow < or & in entity replacement text (or ]]>???)
        // TODO keepEntityNormalizedForAttributeValue cached as well ...
    }

    private String newString( char[] cbuf, int off, int len )
    {
        return new String( cbuf, off, len );
    }

    /**
     * simplistic implementation of hash function that has <b>constant</b> time to compute - so it also means
     * diminishing hash quality for long strings but for XML parsing it should be good enough ...
     */
    private static int fastHash(char[] ch, int off, int len )
    {
        if ( len == 0 )
            return 0;
        // assert len >0
        int hash = ch[off]; // hash at beginning
        // try {
        hash = ( hash << 7 ) + ch[off + len - 1]; // hash at the end
        // } catch(ArrayIndexOutOfBoundsException aie) {
        // aie.printStackTrace(); //should never happen ...
        // throw new RuntimeException("this is violation of pre-condition");
        // }
        if ( len > 16 )
            hash = ( hash << 7 ) + ch[off + ( len / 4 )]; // 1/4 from beginning
        if ( len > 8 )
            hash = ( hash << 7 ) + ch[off + ( len / 2 )]; // 1/2 of string size ...
        // notice that hash is at most done 3 times <<7 so shifted by 21 bits 8 bit value
        // so max result == 29 bits so it is quite just below 31 bits for long (2^32) ...
        // assert hash >= 0;
        return hash;
    }

    public static final EntityReplacementMap defaultEntityReplacementMap = new EntityReplacementMap( new String[][] {
        { "nbsp", "\u00a0" }, { "iexcl", "¡" }, { "cent", "¢" }, { "pound", "£" },
        { "curren", "¤" }, { "yen", "¥" }, { "brvbar", "¦" }, { "sect", "§" }, { "uml", "¨" },
        { "copy", "©" }, { "ordf", "ª" }, { "laquo", "«" }, { "not", "¬" }, { "shy", "\u00ad" },
        { "reg", "®" }, { "macr", "¯" }, { "deg", "°" }, { "plusmn", "±" }, { "sup2", "²" },
        { "sup3", "³" }, { "acute", "´" }, { "micro", "µ" }, { "para", "¶" },
        { "middot", "·" }, { "cedil", "¸" }, { "sup1", "¹" }, { "ordm", "º" },
        { "raquo", "»" }, { "frac14", "¼" }, { "frac12", "½" }, { "frac34", "¾" },
        { "iquest", "¿" }, { "Agrave", "À" }, { "Aacute", "Á" }, { "Acirc", "Â" },
        { "Atilde", "Ã" }, { "Auml", "Ä" }, { "Aring", "Å" }, { "AElig", "Æ" },
        { "Ccedil", "Ç" }, { "Egrave", "È" }, { "Eacute", "É" }, { "Ecirc", "Ê" },
        { "Euml", "Ë" }, { "Igrave", "Ì" }, { "Iacute", "Í" }, { "Icirc", "Î" },
        { "Iuml", "Ï" }, { "ETH", "Ð" }, { "Ntilde", "Ñ" }, { "Ograve", "Ò" },
        { "Oacute", "Ó" }, { "Ocirc", "Ô" }, { "Otilde", "Õ" }, { "Ouml", "Ö" },
        { "times", "×" }, { "Oslash", "Ø" }, { "Ugrave", "Ù" }, { "Uacute", "Ú" },
        { "Ucirc", "Û" }, { "Uuml", "Ü" }, { "Yacute", "Ý" }, { "THORN", "Þ" },
        { "szlig", "ß" }, { "agrave", "à" }, { "aacute", "á" }, { "acirc", "â" },
        { "atilde", "ã" }, { "auml", "ä" }, { "aring", "å" }, { "aelig", "æ" },
        { "ccedil", "ç" }, { "egrave", "è" }, { "eacute", "é" }, { "ecirc", "ê" },
        { "euml", "ë" }, { "igrave", "ì" }, { "iacute", "í" }, { "icirc", "î" },
        { "iuml", "ï" }, { "eth", "ð" }, { "ntilde", "ñ" }, { "ograve", "ò" },
        { "oacute", "ó" }, { "ocirc", "ô" }, { "otilde", "õ" }, { "ouml", "ö" },
        { "divide", "÷" }, { "oslash", "ø" }, { "ugrave", "ù" }, { "uacute", "ú" },
        { "ucirc", "û" }, { "uuml", "ü" }, { "yacute", "ý" }, { "thorn", "þ" },
        { "yuml", "ÿ" },

        // ----------------------------------------------------------------------
        // Special entities
        // ----------------------------------------------------------------------

        { "OElig", "Œ" }, { "oelig", "œ" }, { "Scaron", "Š" }, { "scaron", "š" },
        { "Yuml", "Ÿ" }, { "circ", "ˆ" }, { "tilde", "˜" }, { "ensp", "\u2002" }, { "emsp", "\u2003" },
        { "thinsp", "\u2009" }, { "zwnj", "\u200c" }, { "zwj", "\u200d" }, { "lrm", "\u200e" }, { "rlm", "\u200f" },
        { "ndash", "–" }, { "mdash", "—" }, { "lsquo", "‘" }, { "rsquo", "’" },
        { "sbquo", "‚" }, { "ldquo", "“" }, { "rdquo", "”" }, { "bdquo", "„" },
        { "dagger", "†" }, { "Dagger", "‡" }, { "permil", "‰" }, { "lsaquo", "‹" },
        { "rsaquo", "›" }, { "euro", "€" },

        // ----------------------------------------------------------------------
        // Symbol entities
        // ----------------------------------------------------------------------

        { "fnof", "ƒ" }, { "Alpha", "Α" }, { "Beta", "Β" }, { "Gamma", "Γ" }, { "Delta", "Δ" },
        { "Epsilon", "Ε" }, { "Zeta", "Ζ" }, { "Eta", "Η" }, { "Theta", "Θ" }, { "Iota", "Ι" },
        { "Kappa", "Κ" }, { "Lambda", "Λ" }, { "Mu", "Μ" }, { "Nu", "Ν" }, { "Xi", "Ξ" },
        { "Omicron", "Ο" }, { "Pi", "Π" }, { "Rho", "Ρ" }, { "Sigma", "Σ" }, { "Tau", "Τ" },
        { "Upsilon", "Υ" }, { "Phi", "Φ" }, { "Chi", "Χ" }, { "Psi", "Ψ" }, { "Omega", "Ω" },
        { "alpha", "α" }, { "beta", "β" }, { "gamma", "γ" }, { "delta", "δ" },
        { "epsilon", "ε" }, { "zeta", "ζ" }, { "eta", "η" }, { "theta", "θ" }, { "iota", "ι" },
        { "kappa", "κ" }, { "lambda", "λ" }, { "mu", "μ" }, { "nu", "ν" }, { "xi", "ξ" },
        { "omicron", "ο" }, { "pi", "π" }, { "rho", "ρ" }, { "sigmaf", "ς" }, { "sigma", "σ" },
        { "tau", "τ" }, { "upsilon", "υ" }, { "phi", "φ" }, { "chi", "χ" }, { "psi", "ψ" },
        { "omega", "ω" }, { "thetasym", "ϑ" }, { "upsih", "ϒ" }, { "piv", "ϖ" },
        { "bull", "•" }, { "hellip", "…" }, { "prime", "′" }, { "Prime", "″" },
        { "oline", "‾" }, { "frasl", "⁄" }, { "weierp", "℘" }, { "image", "ℑ" },
        { "real", "ℜ" }, { "trade", "™" }, { "alefsym", "ℵ" }, { "larr", "←" },
        { "uarr", "↑" }, { "rarr", "→" }, { "darr", "↓" }, { "harr", "↔" }, { "crarr", "↵" },
        { "lArr", "⇐" }, { "uArr", "⇑" }, { "rArr", "⇒" }, { "dArr", "⇓" }, { "hArr", "⇔" },
        { "forall", "∀" }, { "part", "∂" }, { "exist", "∃" }, { "empty", "∅" },
        { "nabla", "∇" }, { "isin", "∈" }, { "notin", "∉" }, { "ni", "∋" }, { "prod", "∏" },
        { "sum", "∑" }, { "minus", "−" }, { "lowast", "∗" }, { "radic", "√" }, { "prop", "∝" },
        { "infin", "∞" }, { "ang", "∠" }, { "and", "∧" }, { "or", "∨" }, { "cap", "∩" },
        { "cup", "∪" }, { "int", "∫" }, { "there4", "∴" }, { "sim", "∼" }, { "cong", "≅" },
        { "asymp", "≈" }, { "ne", "≠" }, { "equiv", "≡" }, { "le", "≤" }, { "ge", "≥" },
        { "sub", "⊂" }, { "sup", "⊃" }, { "nsub", "⊄" }, { "sube", "⊆" }, { "supe", "⊇" },
        { "oplus", "⊕" }, { "otimes", "⊗" }, { "perp", "⊥" }, { "sdot", "⋅" },
        { "lceil", "⌈" }, { "rceil", "⌉" }, { "lfloor", "⌊" }, { "rfloor", "⌋" },
        { "lang", "〈" }, { "rang", "〉" }, { "loz", "◊" }, { "spades", "♠" }, { "clubs", "♣" },
        { "hearts", "♥" }, { "diams", "♦" } } );

}
