package com.peykasa.authserver.utility;

import org.modelmapper.ModelMapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kamran
 */
public class ObjectMapper {
    private static ModelMapper modelMapper = new ModelMapper();

    public static <S, D> D to(S s, Class<D> clazz) {
        return modelMapper.map(s, clazz);
    }

    public static <S, D> List<D> to(Collection<S> list, Class<D> clazz) {
        return list.stream().map(s -> ObjectMapper.map(s, clazz)).collect(Collectors.toList());
    }

    public static <D> D map(Object source, Class<D> destinationType) {
        return modelMapper.map(source, destinationType);
    }

    public static <S, D> void copy(S source, D dest) {
        modelMapper.map(source, dest);
    }
}
