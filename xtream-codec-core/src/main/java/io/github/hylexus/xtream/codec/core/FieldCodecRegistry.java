package io.github.hylexus.xtream.codec.core;

import io.github.hylexus.xtream.codec.common.utils.XtreamTypes;
import io.github.hylexus.xtream.codec.common.utils.XtreamUtils;
import io.github.hylexus.xtream.codec.core.impl.codec.*;
import io.github.hylexus.xtream.codec.core.type.XtreamDataType;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class FieldCodecRegistry {

    private final Map<String, FieldCodec<?>> mapping = new LinkedHashMap<>();

    public FieldCodecRegistry() {
        init(this);
    }

    static void init(FieldCodecRegistry registry) {
        registry.register(byte.class, XtreamDataType.i8.sizeInBytes(), new I8FieldCodec(), "", false);
        registry.register(Byte.class, XtreamDataType.i8.sizeInBytes(), new I8FieldCodec(), "", false);
        registry.register(short.class, XtreamDataType.u8.sizeInBytes(), new U8FieldCodec(), "", false);
        registry.register(Short.class, XtreamDataType.u8.sizeInBytes(), new U8FieldCodec(), "", false);

        registry.register(short.class, XtreamDataType.i16.sizeInBytes(), new I16FieldCodec(), "", false);
        registry.register(Short.class, XtreamDataType.i16.sizeInBytes(), new I16FieldCodec(), "", false);
        registry.register(int.class, XtreamDataType.u16.sizeInBytes(), new U16FieldCodec(), "", false);
        registry.register(Integer.class, XtreamDataType.u16.sizeInBytes(), new U16FieldCodec(), "", false);

        registry.register(short.class, XtreamDataType.i16_le.sizeInBytes(), new I16FieldCodecLittleEndian(), "", true);
        registry.register(Short.class, XtreamDataType.i16_le.sizeInBytes(), new I16FieldCodecLittleEndian(), "", true);
        registry.register(int.class, XtreamDataType.u16_le.sizeInBytes(), new U16FieldCodecLittleEndian(), "", true);
        registry.register(Integer.class, XtreamDataType.u16_le.sizeInBytes(), new U16FieldCodecLittleEndian(), "", true);

        registry.register(int.class, XtreamDataType.i32.sizeInBytes(), new I32FieldCodec(), "", false);
        registry.register(Integer.class, XtreamDataType.i32.sizeInBytes(), new I32FieldCodec(), "", false);
        registry.register(long.class, XtreamDataType.u32.sizeInBytes(), new U32FieldCodec(), "", false);
        registry.register(Long.class, XtreamDataType.u32.sizeInBytes(), new U32FieldCodec(), "", false);

        registry.register(int.class, XtreamDataType.i32_le.sizeInBytes(), new I32FieldCodecLittleEndian(), "", true);
        registry.register(Integer.class, XtreamDataType.i32_le.sizeInBytes(), new I32FieldCodecLittleEndian(), "", true);
        registry.register(long.class, XtreamDataType.u32_le.sizeInBytes(), new U32FieldCodecLittleEndian(), "", true);
        registry.register(Long.class, XtreamDataType.u32_le.sizeInBytes(), new U32FieldCodecLittleEndian(), "", true);

        registry.register(long.class, XtreamDataType.i64.sizeInBytes(), new I64FieldCodec(), "", false);
        registry.register(Long.class, XtreamDataType.i64.sizeInBytes(), new I64FieldCodec(), "", false);

        registry.register(long.class, XtreamDataType.i64_le.sizeInBytes(), new I64FieldCodecLittleEndian(), "", true);
        registry.register(Long.class, XtreamDataType.i64_le.sizeInBytes(), new I64FieldCodecLittleEndian(), "", true);

        registry.register(float.class, XtreamDataType.f32.sizeInBytes(), new F32FieldCodec(), "", false);
        registry.register(Float.class, XtreamDataType.f32.sizeInBytes(), new F32FieldCodec(), "", false);

        registry.register(float.class, XtreamDataType.f32_le.sizeInBytes(), new F32FieldCodecLittleEndian(), "", true);
        registry.register(Float.class, XtreamDataType.f32_le.sizeInBytes(), new F32FieldCodecLittleEndian(), "", true);

        registry.register(double.class, XtreamDataType.f64.sizeInBytes(), new F64FieldCodec(), "", false);
        registry.register(Double.class, XtreamDataType.f64.sizeInBytes(), new F64FieldCodec(), "", false);

        registry.register(double.class, XtreamDataType.f64_le.sizeInBytes(), new F64FieldCodecLittleEndian(), "", true);
        registry.register(Double.class, XtreamDataType.f64_le.sizeInBytes(), new F64FieldCodecLittleEndian(), "", true);

        registry.register(byte[].class, -1, new ByteArrayFieldCodec(), "", false);
        registry.register(Byte[].class, -1, new ByteBoxArrayFieldCodec(), "", false);

        registerDefaultStringCodec(registry);
    }

    private static void registerDefaultStringCodec(FieldCodecRegistry registry) {
        Arrays.asList(
                Charset.forName("gbk"),
                Charset.forName("gb2312"),
                StandardCharsets.UTF_8,
                StandardCharsets.US_ASCII,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.UTF_16BE,
                StandardCharsets.UTF_16LE,
                StandardCharsets.UTF_16
        ).forEach(charset -> {
            // ...
            registry.register(String.class, XtreamDataType.string.sizeInBytes(), new StringFieldCodec(charset.name()), charset.name(), false);
        });

        registry.register(String.class, XtreamDataType.string.sizeInBytes(), new StringFieldCodec("bcd_8421"), "bcd_8421", false);
    }

    public void register(Class<?> targetType, int sizeInBytes, FieldCodec<?> fieldCodec, String charset, boolean littleEndian) {
        String key = this.generateKey(targetType, sizeInBytes, charset, littleEndian);
        mapping.put(key, fieldCodec);
    }

    protected String generateKey(Class<?> targetType, int sizeInBytes, String charset, boolean littleEndian) {
        if (String.class == targetType) {
            return key(targetType, -1, charset, false);
        } else if (byte[].class == targetType || Byte[].class == targetType) {
            return key(targetType, -1, "", false);
        } else if (targetType == byte.class || targetType == Byte.class) {
            return key(targetType, sizeInBytes, "", false);
        } else {
            return key(targetType, sizeInBytes, "", littleEndian);
        }
    }

    public Optional<FieldCodec<?>> getFieldCodec(Class<?> targetType, int sizeInBytes, String charset, boolean littleEndian) {
        final String key = generateKey(targetType, sizeInBytes, charset, littleEndian);
        return Optional.ofNullable(mapping.get(key));
    }

    String key(Class<?> targetType, int length, String charset, boolean littleEndian) {
        final String numberTips = (targetType != byte.class && targetType != Byte.class && XtreamTypes.isNumberType(targetType))
                ? (littleEndian ? "(LE)" : "(BE)")
                : "";
        if (XtreamUtils.hasElement(charset)) {
            return targetType.getName() + numberTips + " <--> byte[" + (length <= 0 ? "n" : length) + "] (" + (charset.toLowerCase()) + ")";
        }
        return targetType.getName() + numberTips + " <--> byte[" + (length <= 0 ? "n" : length) + "]";
    }

}
