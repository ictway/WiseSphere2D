package org.deegree.feature.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.feature.types.property.ObjectPropertyType;
import org.deegree.gml.schema.GMLSchemaInfoSet;

/**
 * Defines {@link GMLObjectType}s (e.g. feature types) and their derivation hierarchy.
 * <p>
 * May be based on a {@link GMLSchemaInfoSet}. If it is build from a GML schema, {@link #getGMLSchema()} can be used to
 * access the full schema infoset.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface AppSchema {

    /**
     * Returns all feature types that are defined in this application schema.
     * 
     * @return all feature types, never <code>null</code>
     */
    FeatureType[] getFeatureTypes();

    /**
     * Returns all feature types that are defined in this application schema, limited by the options.
     * 
     * @param namespace
     *            may be <code>null</code> (include all feature types from all namespaces)
     * @param includeCollections
     *            set to <code>true</code>, if feature collection types shall be included, <code>false</code> otherwise
     * @param includeAbstracts
     *            set to <code>true</code>, if abstract types shall be included, <code>false</code> otherwise
     * 
     * @return all feature types, never <code>null</code>
     */
    List<FeatureType> getFeatureTypes( String namespace, boolean includeCollections, boolean includeAbstracts );

    /**
     * Returns all root feature types that are defined in this application schema.
     * 
     * @return all root feature types, never <code>null</code>
     */
    FeatureType[] getRootFeatureTypes();

    /**
     * Retrieves the feature type with the given name.
     * 
     * @param ftName
     *            feature type name to look up, must not be <code>null</code>
     * @return the feature type with the given name, or <code>null</code> if no such feature type exists
     */
    FeatureType getFeatureType( QName ftName );

    /**
     * Retrieves the direct subtypes for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return the direct subtypes of the given feature type (abstract and non-abstract)
     */
    FeatureType[] getDirectSubtypes( FeatureType ft );

    /**
     * Retrieves the parent feature type for the specified feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return parent feature type, can be <code>null</code>
     */
    FeatureType getParent( FeatureType ft );

    /**
     * Retrieves all substitutions (abstract and non-abstract ones) for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return all substitutions for the given feature type, never <code>null</code>
     */
    FeatureType[] getSubtypes( FeatureType ft );

    /**
     * Retrieves all concrete substitutions for the given feature type.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return all concrete substitutions for the given feature type, never <code>null</code>
     */
    FeatureType[] getConcreteSubtypes( FeatureType ft );

    /**
     * Returns the underlying {@link GMLSchemaInfoSet}
     * 
     * @return the underlying GML schema, can be <code>null</code> (not based on a GML schema)
     */
    GMLSchemaInfoSet getGMLSchema();

    /**
     * Determines whether a feature type is substitutable for another feature type.
     * <p>
     * This is true, iff <code>substitution</code> is either:
     * <ul>
     * <li>equal to <code>ft</code></li>
     * <li>a direct subtype of <code>ft</code></li>
     * <li>a transititive subtype of <code>ft</code></li>
     * </ul>
     * 
     * @param ft
     *            base feature type, must be part of this schema
     * @param substitution
     *            feature type to be checked, must be part of this schema
     * @return <code>true</code>, if the second feature type is a valid substitution for the first one
     */
    boolean isSubType( FeatureType ft, FeatureType substitution );

    /**
     * Returns the {@link PropertyType}s from the specified {@link FeatureType} declaration that are *not* present in
     * the parent {@link FeatureType} or its ancestors.
     * 
     * @param ft
     *            feature type, must not be <code>null</code>
     * @return list of property declarations, may be empty, but never <code>null</code>
     */
    List<PropertyType> getNewPropertyDecls( FeatureType ft );

    Map<FeatureType, FeatureType> getFtToSuperFt();

    /**
     * Returns the preferred namespace bindings for all namespaces.
     * 
     * @return the preferred namespace bindings for all namespaces, never <code>null</code>
     */
    Map<String, String> getNamespaceBindings();

    /**
     * Returns the {@link XSElementDeclaration}s/{@link XSWildcard}s that the given complex type definition allows for.
     * <p>
     * TODO: Respect order and cardinality of child elements.
     * </p>
     * 
     * @param type
     *            complex type definition, must not be <code>null</code>
     * @return allowed child element declarations, never <code>null</code>
     */
    Map<QName, XSTerm> getAllowedChildElementDecls( XSComplexTypeDefinition type );

    /**
     * Returns the application namespaces.
     * <p>
     * NOTE: This excludes the GML core namespaces.
     * </p>
     * 
     * @return the application namespaces, never <code>null</code>
     */
    Set<String> getAppNamespaces();

    /**
     * Returns the namespaces that the definitions in the given namespace depend upon (excluding transitive
     * dependencies).
     * 
     * @param ns
     *            application namespace, must not be <code>null</code>
     * @return namespace dependencies, may be empty, but never <code>null</code>
     */
    List<String> getNamespacesDependencies( String ns );

    /**
     * Returns the {@link ObjectPropertyType} for the given element declaration (if it defines an object property).
     * 
     * @param elDecl
     *            element declaration, must not be <code>null</code>
     * @return property declaration or <code>null</code> (if the element does not declare an {@link ObjectPropertyType})
     */
    ObjectPropertyType getCustomElDecl( XSElementDeclaration elDecl );

    /**
     * Returns all geometry types that are defined in this application schema.
     * 
     * @return all geometry types, never <code>null</code>
     */
    List<GMLObjectType> getGeometryTypes();

    /**
     * Retrieves the geometry type declaration with the given name.
     * 
     * @param ftName
     *            geometry type name to look up, must not be <code>null</code>
     * @return the geometry type with the given name, or <code>null</code> if no such geometry type exists
     */
    GMLObjectType getGeometryType( QName name );

    List<GMLObjectType> getSubstitutions( QName name );

    List<GMLObjectType> getDirectSubstitutions( QName name );

    AppSchemaGeometryHierarchy getGeometryHierarchy();

    Map<GMLObjectType, GMLObjectType> getGeometryToSuperType();
}
