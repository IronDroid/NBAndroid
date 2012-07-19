/*
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
package org.netbeans.modules.android.grammars;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.android.core.sdk.DalvikPlatform;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.util.Enumerations;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Shared code for query implementation user for android XML files.
 *
 * @author Radim Kubacki
 */
abstract class AndroidGrammar implements GrammarQuery {

  private static final Logger LOG = Logger.getLogger(AndroidGrammar.class.getName());

  protected final DalvikPlatform platform;

  protected final StyleableModel model;
  private final ReferenceResolver refResolver;

  public AndroidGrammar(DalvikPlatform platform, StyleableModel model, ReferenceResolver refResolver) {
    this.platform = Preconditions.checkNotNull(platform);
    this.model = Preconditions.checkNotNull(model);
    this.refResolver = Preconditions.checkNotNull(refResolver);
  }

  @VisibleForTesting
  StyleableModel getStyleableModel() {
    return model;
  }

  /**
   * Allow to get names of <b>parsed general entities</b>.
   * @return list of <code>CompletionResult</code>s (ENTITY_REFERENCE_NODEs)
   */
  @Override
  public final Enumeration<GrammarResult> queryEntities(String prefix) {
    List<GrammarResult> list = new ArrayList<GrammarResult>();

    // XXX(radim): add well-know build-in entity names, can we do better?

    if ("lt".startsWith(prefix)) {
      list.add(new SimpleEntityReference("lt"));
    }
    if ("gt".startsWith(prefix)) {
      list.add(new SimpleEntityReference("gt"));
    }
    if ("apos".startsWith(prefix)) {
      list.add(new SimpleEntityReference("apos"));
    }
    if ("quot".startsWith(prefix)) {
      list.add(new SimpleEntityReference("quot"));
    }
    if ("amp".startsWith(prefix)) {
      list.add(new SimpleEntityReference("amp"));
    }

    LOG.log(Level.FINE, "queryEntities({0}) -> {1}", new Object[] { prefix, list });
    return Collections.enumeration(list);
  }

  @Override
  public Enumeration<GrammarResult> queryAttributes(HintContext ctx) {
    LOG.log(Level.FINE, "queryAttributes({0})", ctx.getCurrentPrefix());

    Element ownerElement = null;
    // Support both versions of GrammarQuery contract
    if (ctx.getNodeType() == Node.ATTRIBUTE_NODE) {
      ownerElement = ((Attr) ctx).getOwnerElement();
    } else if (ctx.getNodeType() == Node.ELEMENT_NODE) {
      ownerElement = (Element) ctx;
    }
    if (ownerElement == null) {
      return Enumerations.empty();
    }

    String prefix = ctx.getCurrentPrefix();
    return doQueryAttributes(ownerElement, prefix);
  }

  protected abstract Enumeration<GrammarResult> doQueryAttributes(Element ownerElement, String prefix);

  @Override
  public final Enumeration<GrammarResult> queryNotations(String prefix) {
    LOG.log(Level.FINE, "queryNotatios({0})", prefix);
    return Enumerations.empty();
  }

  // it is not yet implemented
  @Override
  public final boolean isAllowed(Enumeration<GrammarResult> en) {
    return true;
  }

  // customizers section ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  @Override
  public final java.awt.Component getCustomizer(HintContext ctx) {
    return null;
  }

  @Override
  public final boolean hasCustomizer(HintContext ctx) {
    return false;
  }

  @Override
  public final org.openide.nodes.Node.Property[] getProperties(HintContext ctx) {
    return null;
  }

  private static Predicate<String> startsWithPredicate(final String prefix) {
      return new Predicate<String>(){
          @Override public boolean apply(String input) {
            return input.startsWith(prefix);
          }

        };
  }

  protected Iterable<String> getChoices(AttributeInfo attr, final String prefix) {
    Iterable<String> values = Collections.emptySet();
    if (Iterables.contains(attr.getFormats(), AttributeInfo.Format.BOOLEAN)) {
      values = Iterables.concat(values, Lists.newArrayList("true", "false"));
    }
    if (Iterables.contains(attr.getFormats(), AttributeInfo.Format.ENUM)) {
      values = Iterables.concat(
          values,
          Iterables.filter(attr.getEnumValues(), startsWithPredicate(prefix)));
    }
    if (Iterables.contains(attr.getFormats(), AttributeInfo.Format.DIMENSION)) {
      int i = 0;
      while (i < prefix.length() && Character.isDigit(prefix.charAt(i))) {
        i++;
      }
      if (i > 0) {
        List<String> dimensions = Lists.newArrayList();
        String number = prefix.substring(0, i);
        String unitPrefix = i < prefix.length() ? prefix.substring(i) : "";
        for (String unit : new String[] {"dp", "sp", "pt", "mm", "in", "px"}) {
          if (unit.startsWith(unitPrefix)) {
            dimensions.add(number + unit);
          }
        }
        values = Iterables.concat(values, dimensions);
      }
    }
    if (Iterables.contains(attr.getFormats(), AttributeInfo.Format.REFERENCE)) {
      Iterable<String> offeredValues = Collections.emptyList();
      if (prefix.startsWith("@") && prefix.indexOf('/') > 0) {
        offeredValues = Iterables.transform(
            Iterables.filter(
                refResolver.getReferences(),
                new Predicate<ResourceRef>() {
                  @Override
                  public boolean apply(ResourceRef input) {
                    if (input.toString().startsWith(prefix)) {
                      return true;
                    }
                    return false;
                  }
                }),
            Functions.toStringFunction());
      } else if (prefix.startsWith("@")) {
        final String valuePrefix = prefix.startsWith("@+") ? "@+" : "@";
        offeredValues = Sets.newTreeSet(Iterables.transform(
            Iterables.filter(
                refResolver.getReferences(),
                new Predicate<ResourceRef>() {
                  @Override
                  public boolean apply(ResourceRef input) {
                    if ((valuePrefix + input.resourceType).startsWith(prefix)) {
                      return true;
                    }
                    return false;
                  }
                }),
            new Function<ResourceRef, String>() {
              @Override
              public String apply(ResourceRef input) {
                return valuePrefix + input.resourceType + "/";
              }
            }));
      }
      values = Iterables.concat(
          values, offeredValues);
    }
    return Iterables.filter(values, startsWithPredicate(prefix));
  }
}
