package top.mrxiaom.pluginbase.resolver.plexus.interpolation;

/*
 * Copyright 2001-2008 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.mrxiaom.pluginbase.resolver.plexus.interpolation.util.StringUtils;

/**
 * Expansion of the original RegexBasedInterpolator, found in plexus-utils, this
 * interpolator provides options for setting custom prefix/suffix regex parts,
 * and includes a {@link RecursionInterceptor} parameter in its interpolate(..)
 * call, to allow the detection of cyclical expression references.
 *
 */
public class RegexBasedInterpolator
    implements Interpolator
{
    private final Map<String, Object> existingAnswers = new HashMap<>();

    private final List<ValueSource> valueSources = new ArrayList<>();
    
    private final List<InterpolationPostProcessor> postProcessors = new ArrayList<>();
    
    private boolean reusePatterns = false;
    
    private boolean cacheAnswers = false;
    
    public static final String DEFAULT_REGEXP = "\\$\\{(.+?)}";
    
    /**
     * the key is the regex the value is the Pattern 
     * At the class construction time the Map will contains the default Pattern
     */
    private final Map<String, Pattern> compiledPatterns = new WeakHashMap<>();
    
    /**
     * Setup a basic interpolator.
     * <p><b>NOTE:</b> You will have to call</p>
     * {@link RegexBasedInterpolator#addValueSource(ValueSource)} at least once
     * if you use this constructor!
     */
    public RegexBasedInterpolator()
    {
        compiledPatterns.put( DEFAULT_REGEXP, Pattern.compile( DEFAULT_REGEXP ) );
    }

    /**
     * {@inheritDoc}
     */
    public void addValueSource( ValueSource valueSource )
    {
        valueSources.add( valueSource );
    }

    /**
     * {@inheritDoc}
     */
    public void addPostProcessor( InterpolationPostProcessor postProcessor )
    {
        postProcessors.add( postProcessor );
    }

    /**
     * Attempt to resolve all expressions in the given input string, using the
     * given pattern to first trim an optional prefix from each expression. The
     * supplied recursion interceptor will provide protection from expression
     * cycles, ensuring that the input can be resolved or an exception is
     * thrown.
     *
     * @param input The input string to interpolate
     *
     * @param thisPrefixPattern An optional pattern that should be trimmed from
     *                          the start of any expressions found in the input.
     *
     * @param recursionInterceptor Used to protect the interpolation process
     *                             from expression cycles, and throw an
     *                             exception if one is detected.
     */
    public String interpolate( String input,
                               String thisPrefixPattern,
                               RecursionInterceptor recursionInterceptor )
        throws InterpolationException
    {
        if (input == null )
        {
            // return empty String to prevent NPE too
            return "";
        }
        if ( recursionInterceptor == null )
        {
            recursionInterceptor = new SimpleRecursionInterceptor();
        }

        if ( thisPrefixPattern != null && thisPrefixPattern.isEmpty())
        {
            thisPrefixPattern = null;
        }

        int realExprGroup = 2;
        Pattern expressionPattern;

        if ( thisPrefixPattern != null )
        {
            expressionPattern = getPattern( "\\$\\{(" + thisPrefixPattern + ")?(.+?)\\}" );
        }
        else
        {
            expressionPattern = getPattern( DEFAULT_REGEXP );
            realExprGroup = 1;
        }

        try
        {
            return interpolate( input, recursionInterceptor, expressionPattern, realExprGroup );
        }
        finally
        {
            if ( !cacheAnswers )
            {
                clearAnswers();
            }
        }
    }
    
    private Pattern getPattern( String regExp )
    {
        if ( !reusePatterns )
        {
            return Pattern.compile( regExp );
        }
           
        Pattern pattern;
        synchronized( this )
        {
            pattern = compiledPatterns.get( regExp );
            
            if ( pattern != null )
            {
                return pattern;
            }

            pattern = Pattern.compile( regExp );
            compiledPatterns.put( regExp, pattern );
        }
        
        return pattern;
    }

    /**
     * Entry point for recursive resolution of an expression and all of its
     * nested expressions.
     */
    private String interpolate( String input,
                                RecursionInterceptor recursionInterceptor,
                                Pattern expressionPattern,
                                int realExprGroup )
        throws InterpolationException
    {
        if (input == null )
        {
            // return empty String to prevent NPE too
            return "";
        }        
        String result = input;
        
        Matcher matcher = expressionPattern.matcher( result );

        while ( matcher.find() )
        {
            String wholeExpr = matcher.group( 0 );
            String realExpr = matcher.group( realExprGroup );

            if ( realExpr.startsWith( "." ) )
            {
                realExpr = realExpr.substring( 1 );
            }

            if ( recursionInterceptor.hasRecursiveExpression( realExpr ) )
            {
                throw new InterpolationCycleException( recursionInterceptor, realExpr, wholeExpr );
            }

            recursionInterceptor.expressionResolutionStarted( realExpr );
            try
            {
                Object value = existingAnswers.get( realExpr );
                for ( ValueSource vs : valueSources )
                {
                   if (value != null) break;

                   value = vs.getValue( realExpr );
                }

                if ( value != null )
                {
                    value =
                        interpolate( String.valueOf( value ), recursionInterceptor, expressionPattern, realExprGroup );

                    if ( !postProcessors.isEmpty() )
                    {
                        for ( InterpolationPostProcessor postProcessor : postProcessors )
                        {
                            Object newVal = postProcessor.execute( realExpr, value );
                            if ( newVal != null )
                            {
                                value = newVal;
                                break;
                            }
                        }
                    }

                    // could use:
                    // result = matcher.replaceFirst( stringValue );
                    // but this could result in multiple lookups of stringValue, and replaceAll is not correct behaviour
                    result = StringUtils.replace( result, wholeExpr, String.valueOf( value ) );

                    matcher.reset( result );
                }
            }
            finally
            {
                recursionInterceptor.expressionResolutionFinished( realExpr );
            }
        }
        
        return result;
    }

    /**
     * See {@link RegexBasedInterpolator#interpolate(String, String, RecursionInterceptor)}.
     * <p>
     * This method triggers the use of a {@link SimpleRecursionInterceptor}
     * instance for protection against expression cycles.</p>
     *
     * @param input The input string to interpolate
     *
     * @param thisPrefixPattern An optional pattern that should be trimmed from
     *                          the start of any expressions found in the input.
     */
    public String interpolate( String input,
                               String thisPrefixPattern )
        throws InterpolationException
    {
        return interpolate( input, thisPrefixPattern, null );
    }

    /**
     * See {@link RegexBasedInterpolator#interpolate(String, String, RecursionInterceptor)}.
     * <p>
     * This method triggers the use of a {@link SimpleRecursionInterceptor}
     * instance for protection against expression cycles. It also leaves empty the
     * expression prefix which would otherwise be trimmed from expressions. The
     * result is that any detected expression will be resolved as-is.</p>
     *
     * @param input The input string to interpolate
     */
    public String interpolate( String input )
        throws InterpolationException
    {
        return interpolate( input, null, null );
    }

    /**
     * See {@link RegexBasedInterpolator#interpolate(String, String, RecursionInterceptor)}.
     * <p>
     * This method leaves empty the expression prefix which would otherwise be
     * trimmed from expressions. The result is that any detected expression will
     * be resolved as-is.</p>
     *
     * @param input The input string to interpolate
     *
     * @param recursionInterceptor Used to protect the interpolation process
     *                             from expression cycles, and throw an
     *                             exception if one is detected.
     */
    public String interpolate( String input,
                               RecursionInterceptor recursionInterceptor )
        throws InterpolationException
    {
        return interpolate( input, null, recursionInterceptor );
    }

    public boolean isReusePatterns()
    {
        return reusePatterns;
    }

    public void setReusePatterns( boolean reusePatterns )
    {
        this.reusePatterns = reusePatterns;
    }

    public void setCacheAnswers( boolean cacheAnswers )
    {
        this.cacheAnswers = cacheAnswers;
    }
    
    public void clearAnswers()
    {
        existingAnswers.clear();
    }

}
