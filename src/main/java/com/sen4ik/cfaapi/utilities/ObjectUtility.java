package com.sen4ik.cfaapi.utilities;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.FeatureDescriptor;
import java.util.stream.Stream;

public class ObjectUtility {

    // https://stackoverflow.com/questions/19737626/how-to-ignore-null-values-using-springframework-beanutils-copyproperties

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

//    public static String[] getNullPropertyNames(Object source) {
//        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
//        return Stream.of(wrappedSource.getPropertyDescriptors())
//                .map(FeatureDescriptor::getName)
//                .filter(propertyName -> {
//                    try {
//                        return wrappedSource.getPropertyValue(propertyName) == null
//                    } catch (Exception e) {
//                        return false
//                    }
//                })
//                .toArray(String[]::new);
//    }
}
