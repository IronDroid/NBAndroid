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
package org.netbeans.modules.android.grammars.resources;

import com.google.common.collect.Lists;

/**
 * Description of an element used in XML files under {@code res/values}.
 * @author radim
 */
public class ResourcesModel {

  public static final String ROOT_ELEMENT = "resources";
  public static final String NAME_ATTR = "name";
  public static final String TYPE_ATTR = "type";

  private ResElementDescriptor resourcesElement;

  public ResourcesModel() {
//    ResAttrDescriptor nameAttrInfo = new ResAttrDescriptor(NAME_ATTR, null); //, new Format[]{Format.STRING});
    ResElementDescriptor color_element = new ResElementDescriptor(
        "color",
        "A @color@ value specifies an RGB value with an alpha channel, "
        + "which can be used in various places such as specifying a solid color "
        + "for a Drawable or the color to use for text.  "
        + "It always begins with a # character and then is followed by the alpha-red-green-blue information "
        + "in one of the following formats: #RGB, #ARGB, #RRGGBB or #AARRGGBB.",
  //      "http://code.google.com/android/reference/available-resources.html#colorvals",
        Lists.newArrayList(
            new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this color.")), // Text attribute
  //        null /* nsUri */,
  //        nameAttrInfo),
  //        new ColorValueDescriptor(
  //        "Value*",
  //        "A mandatory color value.")
  //      },
        null); // no child nodes
    ResElementDescriptor string_element = new ResElementDescriptor(
        "string",
        "@Strings@, with optional simple formatting, can be stored and retrieved as resources. "
        + "You can add formatting to your string by using three standard HTML tags: b, i, and u. "
        + "If you use an apostrophe or a quote in your string, you must either escape it "
        + "or enclose the whole string in the other kind of enclosing quotes.",
  //      "http://code.google.com/android/reference/available-resources.html#stringresources",
        Lists.newArrayList(
            new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this string.")), // Text attribute
//              null /* nsUri */,
//              nameAttrInfo),
//              new TextValueDescriptor(
//              "Value*",
//              "A mandatory string value.")),
        null); // no child nodes
    ResElementDescriptor item_element = new ResElementDescriptor(
        "item",
        null, // TODO find javadoc
  //      null, // TODO find link to javadoc
        Lists.newArrayList(
            new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this resource."), // Text attribute
  //        null /* nsUri */,
  //        nameAttrInfo),
            new ResAttrDescriptor("type", "The mandatory type of this resource."), // List attribute
  //        null /* nsUri */,
  //        new AttributeInfo(TYPE_ATTR,
  //        new Format[]{Format.STRING, Format.ENUM}).setEnumValues(ResourceType.getNames())),
            new ResAttrDescriptor("format", "The optional format of this resource.")), // flag (bitmask value)
  //        null /* nsUri */,
  //        new AttributeInfo("format",
  //        new Format[]{Format.STRING, Format.FLAG}).setFlagValues(
  //        new String[]{
  //          "boolean",
  //          "color",
  //          "dimension",
  //          "float",
  //          "fraction",
  //          "integer",
  //          "reference",
  //          "string"
  //        })),
  //        new TextValueDescriptor(
  //        "Value",
  //        "A standard string, hex color value, or reference to any other resource type.")
  //      },
        null); // no child nodes
    ResElementDescriptor drawable_element = new ResElementDescriptor(
        "drawable",
        "A @drawable@ defines a rectangle of color. "
        + "Android accepts color values written in various web-style formats -- "
        + "a hexadecimal constant in any of the following forms: #RGB, #ARGB, #RRGGBB, #AARRGGBB. "
        + "Zero in the alpha channel means transparent. The default value is opaque.",
  //      "http://code.google.com/android/reference/available-resources.html#colordrawableresources",
        Lists.newArrayList(
            new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this drawable.")), // Text attribute
  //        null /* nsUri */,
  //        nameAttrInfo),
  //        new TextValueDescriptor(
  //        "Value*",
  //        "A mandatory color value in the form #RGB, #ARGB, #RRGGBB or #AARRGGBB.")
  //      },
        null); // no child nodes
    ResElementDescriptor dimen_element = new ResElementDescriptor(
        "dimen",
        "You can create common dimensions to use for various screen elements by defining @dimension@ values in XML. "
        + "A dimension resource is a number followed by a unit of measurement. "
        + "Supported units are px (pixels), in (inches), mm (millimeters), pt (points at 72 DPI), "
        + "dp (density-independent pixels) and sp (scale-independent pixels)",
  //      "http://code.google.com/android/reference/available-resources.html#dimension",
        Lists.newArrayList(
            new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this dimension.")), // Text attribute
  //        null /* nsUri */,
  //        nameAttrInfo),
  //        new TextValueDescriptor(
  //        "Value*",
  //        "A mandatory dimension value is a number followed by a unit of measurement. For example: 10px, 2in, 5sp.")
  //      },
        null); // no child nodes
    ResElementDescriptor style_element = new ResElementDescriptor(
        "style",
        "Both @styles and themes@ are defined in a style block containing one or more string or numerical values "
        + "(typically color values), or references to other resources (drawables and so on).",
  //      "http://code.google.com/android/reference/available-resources.html#stylesandthemes",
        Lists.newArrayList(
            new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this theme."), // Text attribute
  //        null /* nsUri */,
  //        nameAttrInfo),
            new ResAttrDescriptor("parent",
                    "An optional parent theme. "
                    + "All values from the specified theme will be inherited into this theme. "
                    + "Any values with identical names that you specify will override inherited values.")),
  //        null /* nsUri */,
  //        new AttributeInfo("parent",
  //        new Format[]{Format.STRING})),},
        Lists.newArrayList(
          new ResElementDescriptor(
              "item",
              "A value to use in this @theme@. It can be a standard string, a hex color value, or a reference to any other resource type.",
//              "http://code.google.com/android/reference/available-resources.html#stylesandthemes",
              Lists.newArrayList(
                  new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this item.")), // text attr
//                null /* nsUri */,
//                nameAttrInfo),
//                new TextValueDescriptor(
//                "Value*",
//                "A mandatory standard string, hex color value, or reference to any other resource type.")
//              },
              null))
        );
    ResElementDescriptor string_array_element = new ResElementDescriptor(
        "string-array",
        "An array of strings. Strings are added as underlying item elements to the array.",
//        null, // tooltips
        Lists.newArrayList(
            new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this string array.")), // Text attribute
  //        null /* nsUri */,
  //        nameAttrInfo),},
        Lists.newArrayList(
            new ResElementDescriptor(
            "item",
            "A string value to use in this string array.",
            null, // tooltip
//            new ResAttrDescriptor[]{
//              new TextValueDescriptor(
//              "Value*",
//              "A mandatory string.")
//            },
            null // no child nodes
            )
        ));
    ResElementDescriptor integer_array_element = new ResElementDescriptor(
        "integer-array",
        "An array of integers. Integers are added as underlying item elements to the array.",
        Lists.newArrayList(
            new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this integer array.")), // Text attribute
  //        null /* nsUri */,
  //        nameAttrInfo),},
        Lists.newArrayList(
            new ResElementDescriptor(
                "item",
                "An integer value to use in this integer array.",
                null,
//                      null, // tooltip
//                new ResAttrDescriptor[]{
//                  new TextValueDescriptor(
//                  "Value*",
//                  "A mandatory integer.")
//                },
                null)
        ));
    // surprisingly eclipse is missing this
    ResElementDescriptor typed_array_element = new ResElementDescriptor(
        "array",
        "A type array. Referenced using the value provided in the name attribute.",
        Lists.newArrayList(
            new ResAttrDescriptor(NAME_ATTR, "The mandatory name used in referring to this typed array.")), // Text attribute
  //        null /* nsUri */,
  //        nameAttrInfo),},
        Lists.newArrayList(
            new ResElementDescriptor(
                "item",
                "A generic resource. The value can be a reference to a resource or a simple data type.",
                null,
                null)
        ));
    resourcesElement  = new ResElementDescriptor(
        ROOT_ELEMENT,
        null,
//        "http://code.google.com/android/reference/available-resources.html",
        null, // no attributes
        Lists.newArrayList(
          string_element,
          color_element,
          dimen_element,
          drawable_element,
          style_element,
          item_element,
          string_array_element,
          integer_array_element,
          typed_array_element));
  }

  public ResElementDescriptor getRootElement() {
    return resourcesElement;
  }
}
