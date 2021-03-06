# ttv

Timed Text Markup Language (TTML) Verification Tools

The `ttv` tool is used to verify the contents of a [TTML](http://www.w3.org/TR/ttml1/) document represented using XML as a concrete encoding, and where this document is verified against a specific verification *model*.

Verification is performed in four phases in the following order:

 1. Verify resource presence, encoding, and decodability (into characters).
 2. Verify well-formedness.
 3. Verify schema validity.
 4. Verify semantic validity.

If errors occur in some phase, then the following phases are skipped.

The first phase, resource verification, attempts to determine (sniff) the character encoding of the resource. To do this, it uses a combination of a BOM (byte order mark) prefix, if present, and an XML encoding declaration, if present. If neither are present (or discernable), then it treats the result as ASCII. Once the encoding has been sniffed, an attempt is made to decode the entire resource using that encoding. If no encoding can be determined, is determined but not supported (by the platform), is determined but not permitted (by TTML), or if the resource cannot be successfully decoded without error using the encoding, then this phase fails.

The encodings which `ttv` can successfully resolve include:

 * `US-ASCII`
 * `UTF-8`
 * `UTF-16LE`
 * `UTF-16BE`
 * `UTF-32LE`
 * `UTF-32BE`

If both BOM is present and XML declaration is present and the two encodings differ, then the BOM encoding is used. If desired, the resource's character encoding may
be overridden by using the `--force-encoding` option. If used, then no encoding sniffing occurs, and any internal XML encoding declaration is ignored in favor of the
overriding encoding.

The second phase performs well-formedness testing by attempting to parse the resource as XML but with validation disabled. If the syntax of the resource does not adhere to the XML specification, then this phase fails.

The third phase performs schema validity testing using an XSD (XML Schema Definition) schema. The schema used during validation is determined by the specified (or default) verification model. Different models may specify different schemas. If a schema validation error occurs, then this phase fails.

Unless `--treat-foreign-as` is specified as `allow`, elements and attributes in foreign (non-TTML and non-XML) namespaces are pruned by a filtering process before attempting schema validation. In this case, potential validity of foreign namespace vocabulary is not assessed. If `--treat-foreign-as` is specified as `allow`, then foreign vocabulary is not pruned, which, depending on the schema, may result in schema validation errors if the schema is not designed to permit foreign attributes or elements.

The fourth phase performs additional semantic (and some syntactic) testing on the resource that is beyond what is tested by the previous three phases. For example, some TTML attributes are typed by the TTML schema as `xs:string`, which does not test the form of that string to determine compliance with TTML. In this fourth phase, these attributes are further verified to determine if they comply with TTML syntactic and semantic rules. In addition, a variety of other constraints specified by TTML are tested during this phase.

If desired, the `--until-phase` option may be used to cause verification to stop at a particular phase, thus skipping subsequent phases.

The following `xs:string` schema typed attributes are tested for syntactic correctness during this fourth phase:

 * `tts:color`
 * `tts:backgroundColor`
 * `tts:extent`
 * `tts:fontFamily`
 * `tts:fontSize`
 * `tts:lineHeight`
 * `tts:origin`
 * `tts:padding`
 * `tts:textOutline`
 * `tts:zIndex`

In addition, the following similarly typed attributes are similarly verified in this phase:

 * `begin`
 * `dur`
 * `end`
 * `ttp:cellResolution`
 * `ttp:frameRateMultiplier`
 * `ttp:pixelAspectRatio`

## Additional Semantic Tests

A number of additional semantic constraints are tested in the fourth phase:

 * Warn if neither ttp:profile attribute nor ttp:profile element are present and `missing-profile` warning is enabled. (3.3)
 * Warn if ttp:profile attribute is ignored due to presence of ttp:profile element and `ignored-profile-attribute` warning is not disabled. (5.2)
 * Warn if 'use' attribute on ttp:profile element specifies a non-standard profile designation and `references-non-standard-profile` warning is enabled. (6.1.1)
 * Error if xml:base on ttp:features is not absolute. (6.1.2)
 * Error if xml:base on ttp:features is specified and is not the TT Feature Namespace. (6.1.2)
 * Error if absolutized feature designation doesn't conform to feature-designation defined in D.1. (6.1.3)
 * Error if absolutized feature designation is not a known standard designation. (6.1.3)
 * Error if xml:base on ttp:extensions is not absolute. (6.1.4)
 * Warn if xml:base on ttp:extensions is specified, is not TT Extension Namespace, and `references-other-extension-namespace` warning is enabled. (6.1.4)
 * Error if absolutized extension designation doesn't conform to extension-designation defined in E.1. (6.1.5)
 * Error if absolutized extension designation is not a known standard designation. (6.1.5)
 * Warn if absolutized extension designation is in Other Extension Namespace and `references-non-standard-extension` warning is enabled. (6.1.5)
 * Warn if ttp:profile attribute specifies a non-standard profile designation and `references-non-standard-profile` warning is enabled. (6.2.8)
 * Error if style attribute IDREF does not reference a style element. (8.2.1)
 * Error if style attribute IDREF references a style element that is not a descendant of styling element. (8.2.1)
 * Warn if same IDREF appears more than once in specified tts:style attribute without intervening IDREF and `duplicate-idref-in-style-no-intervening` warning is not disabled. (8.2.1)
 * Warn if same IDREF appears more than once in specified tts:style attribute and `duplicate-idref-in-style` warning is enabled. (8.2.1)
 * Error if tts:extent attribute uses a negative length value. (8.2.7)
 * Error if tts:extent on root (tt) element specifies non-pixel unit for either width or height. (8.2.7)
 * Warn if tts:fontFamily attribute uses quoted generic family name and `quoted-generic-font-family` warning is enabled. (8.2.8)
 * Error if tts:fontSize attribute uses a negative length value. (8.2.9)
 * Error if tts:fontSize attribute uses different units when two length expressions are specified. (8.2.9)
 * Error if tts:lineHeight attribute uses a negative length value. (8.2.12)
 * Warn if tts:opacity attribute uses a legal xs:float value outside the range [0,1] and `out-of-range-opacity` warning is not disabled. (8.2.13)
 * Warn if tts:origin attribute uses a negative length value and `negative-origin` warning is enabled. (8.2.14)
 * Error if tts:padding attribute uses a negative length value. (8.2.16)
 * Error if tts:textOutline attribute uses a negative length value for thickness or blur radius. (8.2.20)
 * Error if loop in sequence of chained style references. (8.4.1.3)
 * Error if region attribute IDREF does not reference a region element. (9.2.1)
 * Error if ttp:clockMode is smpte, ttp:markerMode is discontinuous, and an otherwise well-formed dur attribute is specified. (10.2.3)
 * Error if time expression expressed as clock-time and minutes are greater than 59 (10.3.1)
 * Error if time expression expressed as clock-time and seconds (including fractional part) are greater than 60 (10.3.1)
 * Error if time expression expressed as clock-time and frames are not less than frame rate. (10.3.1)
 * Error if time expression expressed as clock-time and sub-frames are not less than sub-frame rate. (10.3.1)
 * Error if time expression uses frame component or `f` metric when tts:timeBase is clock. (10.3.1)
 * Error if set element specifies zero or more than one style attribute. (11.1.1)
 * Warn if ttm:agent element does not contain a ttm:name child and `missing-agent-name` warning is not disabled. (12.1.5)
 * Warn if ttm:agent element does not contain a ttm:actor child when type='character' and `missing-agent-actor` warning is not disabled. (12.1.5)
 * Error if ttm:actor element's agent attribute does not reference a ttm:agent element. (12.1.7)
 * Error if ttm:actor element's agent attribute does not reference a significant ttm:agent element. (12.1.7)
 * Error if ttm:agent attribute IDREF does not reference a ttm:agent element. (12.2.1)
 * Error if ttm:agent attribute IDREF does not reference a significant ttm:agent element. (12.2.1)
 * Warn if same IDREF appears more than once in specified ttm:agent attribute and `duplicate-idref-in-agent` is enabled. (12.2.1)
 * Warn if same token appears more than once in specified ttm:role attribute and `duplicate-role` warning is not disabled. (12.2.2)
 * Warn if ttm:role attribute references "x-" extension role and `references-extension-role` warning is enabled. (12.2.2)
 * Error if unknown attribute in TT Metadata Namespace specified on any element.
 * Error if TT Metadata Namespace attribute specified on element where it is not explicitly permitted.
 * Error if unknown attribute in TT Parameter Namespace specified on any element.
 * Error if TT Parameter Namespace attribute specified on element where it is not explicitly permitted.
 * Error if unknown attribute in TT StyleMetadata Namespace specified on any element.
 * Error if TT Style Namespace attribute specified on element where it is not explicitly permitted.

If one of the **smpte** verification models applies, a number of additional semantic constraints are tested in the fourth phase:

 * Error if smpte:backgroundImage specifies a local fragment that does not reference a smpte:image element.
 * Warn if smpte:backgroundImage references a non-local image and `references-external-image` warning is enabled.
 * Error if smpte:backgroundImageHorizontal attribute specifies value that is not `left|center|right` or doesn't conform to the TTML \<length\> syntax.
 * Error if smpte:backgroundImageHorizontal attribute uses a negative length value.
 * Error if smpte:backgroundImageVertical attribute specifies value that is not `top|center|bottom` or doesn't conform to the TTML \<length\> syntax.
 * Error if smpte:backgroundImageVertical attribute uses a negative length value.
 * Error if smpte:data element's immediate ancestor is not tt:metadata.
 * Error if smpte:data element's content does not conform to Base64 encoding, ignoring XML whitespace.
 * Error if smpte:data element's datatype attribute is not a standard datatype or private ('x-' prefixed) datatype.
 * Error if smpte:image element's immediate ancestor is not tt:metadata.
 * Error if smpte:image element's content does not conform to Base64 encoding, ignoring XML whitespace.
 * Error if smpte:information element's immediate ancestors are not tt:metadata and tt:head.
 * Error if smpte:information element appears more than once in document.
 * Error if unknown element in some SMPTE-TT Namespace is specified.
 * Error if unknown attribute in some SMPTE-TT Namespace is specified on any element.
 * Error if SMPTE-TT Namespace attribute specified on element where it is not explicitly permitted.
 * Error if SMPTE attribute is empty, all whitespace, or whitespace padded (unless explicitly permitted).

If the **nflxtt** verification models applies, a number of additional semantic constraints are tested in the fourth phase:

 * Error if document encoding is not UTF-8.
 * Error if root element is not tt:tt.
 * Error if tt:tt element does not specify ttp:profile attribute.
 * Error if ttp:profile attribute is not `http://www.netflix.com/ns/ttml/profile/nflx-cc` or `http://www.netflix.com/ns/ttml/profile/nflx-sdh`.
 * Error if tt:tt element does not specify ttp:cellResolution attribute and a cell (c) unit is used in document.
 * Error if tt:tt element does not specify ttp:extent attribute and a pixel (px) unit is used in document.
 * Error if tt:tt element does specify ttp:markerMode attribute.
 * Error if tt:head element is missing.
 * Error if tt:body element is missing.
 * Error if tt:region element specifies tts:overflow attribute.
 * Error if tt:region element specifies tts:origin attribute with value other than `10% 10%` when profile is `http://www.netflix.com/ns/ttml/profile/nflx-sdh`.
 * Error if tt:region element specifies tts:extent attribute with value other than `80% 80%` when profile is `http://www.netflix.com/ns/ttml/profile/nflx-sdh`.
 * Error if smpte:image element is present.
 * Error if smpte:backgroundImage attribute is present.
 * Error if smpte:backgroundImageHorizontal attribute is present.
 * Error if smpte:backgroundImageVertical attribute is present.
 * Error if smpte:backgroundImageVertical attribute is present.
 * Error if \<length\> expression uses em unit.
 * Error if computed geometry of a region is not contained in root container (requires specifying `tts:extent` on `tt:tt` or specifying `--external-extent``).

## Verification Model

A verification *model* includes the following information:

 * a name (exposed via the `--show-models` option)
 * a schema resource name (path), used to locate the schema (XSD) resource
 * a namespace URI string, which must match the target namespace of the schema
 * a JAXB context path, used when creating the JAXB context for unmarshalling
 * a collection of acceptable root JAXB content classes, used to verify the root element
 * additional model specific verification tools used during the semantic verification phase

The model used to perform phase 3 (validity) and phase 4 (semantics) verification is determined as follows: if a `--force-model` option is present, then the force model is used; otherwise, if the resource specifies a `ttva:model` annotation, then the annotated model is used; otherwise, if the `--model` option is present, then it is used; otherwise, the default model is used. In order to determine the default model, invoke **ttv** with the `--show-models` option.

## Building

In order to build **ttv**, run *ant* with one of the following targets:

`ant build`

Using the `build` target will (re)create the `ttv.jar` target under `bld/artifacts`.

`ant clean-build`

Using the `clean-build` target will first clean (delete) previously built classes and artifacts, and then perform a `build`.

The full set of (public) `ant` targets can be listed by using `ant -p`, which presently outputs the following:

<pre>
Main targets:

 build              Build All
 build-servlet      Build TTV Servlet
 build-ttv          Build TTV
 clean-build        Clean and build TTV
 clean-test         Clean, build, and test TTV
 run-valid          Run verifier with valid input test files
 run-valid-verbose  Run verifier with valid input test files, with verbose output.
 test               Run all test suites
 test-apps          Run TTV application tests
 test-ttml1         Run TTML1 tests
 test-ttv           Run all TTV test suites
 test-verifier      Run TTV verification test suite
 test-xml           Run XML tests

Default target: clean-test
</pre>

## Running

In order to run the verifier, use the following (or an equivalent):

`java -jar bld/artifacts/ttv.jar ttml.xml`

Usage information can be obtained by using:

`java -jar bld/artifacts/ttv.jar --help`

At present, this will output the following:

<pre>
Timed Text Verifier (TTV) [1.0.0dev] Copyright 2013-14 Skynav, Inc.
Usage: java -jar ttv.jar [options] URL*
  Short Options:
    -d                                 - see --debug
    -q                                 - see --quiet
    -v                                 - see --verbose
    -?                                 - see --help
  Long Options:
    --debug                            - enable debug output (may be specified multiple times to increase debug level)
    --debug-exceptions                 - enable stack traces on exceptions (implies --debug)
    --disable-warnings                 - disable warnings (both hide and don't count warnings)
    --expect-errors COUNT              - expect count errors or -1 meaning unspecified expectation (default: -1)
    --expect-warnings COUNT            - expect count warnings or -1 meaning unspecified expectation (default: -1)
    --extension-schema NS URL          - add schema for namespace NS at location URL to grammar pool (may be specified multiple times)
    --external-extent EXTENT           - specify extent for document processing context
    --external-frame-rate RATE         - specify frame rate for document processing context
    --force-encoding NAME              - force use of named character encoding, overriding default and resource specified encoding
    --force-model NAME                 - force use of named model, overriding default model and resource specified model
    --help                             - show usage help
    --hide-resource-location           - hide resource location (default: show)
    --hide-resource-path               - hide resource path (default: show)
    --hide-warnings                    - hide warnings (but count them)
    --model NAME                       - specify model name (default: ttml1)
    --no-warn-on TOKEN                 - disable warning specified by warning TOKEN, where multiple instances of this option may be specified
    --no-verbose                       - disable verbose output (resets verbosity level to 0)
    --quiet                            - don't show banner
    --reporter REPORTER                - specify reporter, where REPORTER is null|text|xml (default: text)
    --reporter-file FILE               - specify path to file to which reporter output is to be written
    --reporter-file-encoding ENCODING  - specify character encoding of reporter output (default: utf-8)
    --reporter-file-append             - if reporter file already exists, then append output to it
    --reporter-include-source          - include source context in report messages
    --servlet                          - configure defaults for servlet operation
    --show-models                      - show built-in verification models (use with --verbose to show more details)
    --show-repository                  - show source code repository information
    --show-resource-location           - show resource location (default: show)
    --show-resource-path               - show resource path (default: show)
    --show-validator                   - show platform validator information
    --show-warning-tokens              - show warning tokens (use with --verbose to show more details)
    --verbose                          - enable verbose output (may be specified multiple times to increase verbosity level)
    --treat-foreign-as TOKEN           - specify treatment for foreign namespace vocabulary, where TOKEN is error|warning|info|allow (default: warning)
    --treat-warning-as-error           - treat warning as error (overrides --disable-warnings)
    --until-phase PHASE                - verify up to and including specified phase, where PHASE is none|resource|wellformedness|validity|semantics|all (default: all)
    --warn-on TOKEN                    - enable warning specified by warning TOKEN, where multiple instances of this option may be specified
  Non-Option Arguments:
    URL                                - an absolute or relative URL; if relative, resolved against current working directory
</pre>

As a convenience, if a URL argument takes a relative form, then `ttv` attempts to resolve it against the current working directory.

Run `ttv` with the `--show-models` option to determine which verification models are supported, and which is the default model. If the `-v` option is added, this will additionally show the built-in schemas used by each model, as demonstrated by the following console log:

<pre>
$ java -jar bld/artifacts/ttv.jar -v --show-models
Timed Text Verifier (TTV) [1.0.0dev] Copyright 2013-14 Skynav, Inc.
Verification Models:
  nflxtt
    XSD: xsd/ttml1/ttml1.xsd
    XSD: xsd/smpte/2010/smpte-tt.xsd
    XSD: xsd/smpte/2010/smpte-tt-608.xsd
  st2052-2010
    XSD: xsd/ttml1/ttml1.xsd
    XSD: xsd/smpte/2010/smpte-tt.xsd
    XSD: xsd/smpte/2010/smpte-tt-608.xsd
  st2052-2013
    XSD: xsd/ttml1/ttml1.xsd
    XSD: xsd/smpte/2013/smpte-tt.xsd
    XSD: xsd/smpte/2013/smpte-tt-608.xsd
    XSD: xsd/smpte/2013/smpte-tt-708.xsd
  ttml1 (default)
    XSD: xsd/ttml1/ttml1.xsd
</pre>

## Testing

Junit 4 is used to perform tests on `ttv`. You should have previously installed the appropriate Junit 4 files in your `ant` runtime in order to use these features.

A number of test targets are listed above for invocation from `ant`. The `clean-test` target is useful in order to perform a clean build then run all tests.

In addition, the `run-valid` target will use the command line (not Junit) invocation path in order to run `ttv` on all valid test files included in the Junit testing process.

## Annotations

In order to ease the use of `ttv`, a number of *annotation* attributes are supported to provide verification configuration parameters within a document that would otherwise have to be expressed on the command line, where the namespace of these attributes is `http://skynav.com/ns/ttv/annotations`, and a prefix of `ttva` is used out of convenience (though this can change in an actual document instance). The following annotations are defined, and, if specified, must appear on the `tt:tt` (root) element of a document:

 * expectedErrors
 * expectedWarnings
 * model
 * noWarnOn
 * warnOn

The values of the `ttva:expectedErrors` and `ttva:expectedWarnings` annotations express a non-negative integer, indicating the expected number of errors and warnings to be encountered. When running `ttv` on a resource containing these annotations, a *PASS* result will be reported if the expected and actual numbers of errors and warnings match, and *FAIL* if not matched.

The values of the `ttva:warnOn` and `ttva:noWarnOn` annotations express a whitespace separated listed of warning *TOKENs* to enable or disable, as if they had been specified on the command line.

The value of the `ttva:model` annotation expresses a model name to be used to verify the document.

An example of a document fragment that uses annotations follows (taken from the `ttv` test suite):

<pre>
&lt;tt xml:lang="" xmlns="http://www.w3.org/ns/ttml"
    xmlns:ttp="http://www.w3.org/ns/ttml#parameter"
    ttp:profile="http://www.smpte-ra.org/schemas/2052-1/2010/profiles/smpte-tt-full"
    xmlns:smpte="http://www.smpte-ra.org/schemas/2052-1/2010/smpte-tt"
    xmlns:ttva="http://skynav.com/ns/ttv/annotations"
    ttva:warnOn="all"
    ttva:expectedErrors="1"
    ttva:expectedWarnings="0"
    ttva:model="st2052-2010"&gt;
...
&lt;/tt&gt;
</pre>

## Notes

 * At present, `ttv` is being developed using the following versions of tools:

<pre>
    $ java -version
    java version "1.6.0_65"
    Java(TM) SE Runtime Environment (build 1.6.0_65-b14-462-11M4609)
    Java HotSpot(TM) 64-Bit Server VM (build 20.65-b04-462, mixed mode)

    $ ant -version
    Apache Ant(TM) version 1.9.3 compiled on December 23 2013

    $ java -cp /usr/share/java/junit.jar junit.runner.Version
    4.11
</pre>

 * An (Helios) Eclipse workspace and `ttv` project are available under the `.eclipse` directory.

 * See the `dst` directory for archived source and binary releases.

## Issues

See [Open Issues](http://github.com/skynav/ttv/issues?state=open) for current known bugs, feature requests (enhancements), etc.

