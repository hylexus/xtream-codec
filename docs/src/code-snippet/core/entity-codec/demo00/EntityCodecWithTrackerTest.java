class EntityCodecWithTrackerTest {

    EntityCodec entityCodec = EntityCodec.DEFAULT;

    @Test
    void testEncode() {
        final UserEntity userEntity = new UserEntity()
                .setId(1024L)
                .setName("无名氏")
                .setAge(2048)
                .setAddress("保密");
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        try {
            final CodecTracker tracker = new CodecTracker();
            // 将实体对象编码到 buffer 中
            this.entityCodec.encode(userEntity, buffer, tracker);
            // 访问(遍历)tracker 默认使用 System.out.println 输出到控制台
            tracker.visit();
            final String hexString = FormatUtils.toHexString(buffer);
            assertEquals("0000040009e697a0e5908de6b08f080006e4bf9de5af86", hexString);
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }

    @Test
    void testDecode() {
        final ByteBuf buffer = XtreamBytes.byteBufFromHexString(ByteBufAllocator.DEFAULT, "0000040009e697a0e5908de6b08f080006e4bf9de5af86");
        try {
            final CodecTracker tracker = new CodecTracker();
            // 将数据从 buffer 中解码到实体对象
            final UserEntity entity = this.entityCodec.decode(UserEntity.class, buffer, tracker);
            // 访问(遍历)tracker 默认使用 System.out.println 输出到控制台
            tracker.visit();
            assertEquals(1024L, entity.getId());
            assertEquals("无名氏", entity.getName());
            assertEquals(2048, entity.getAge());
            assertEquals("保密", entity.getAddress());
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt());
        }
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class UserEntity {

        @Preset.RustStyle.u32(desc = "用户ID(32位无符号数)")
        private Long id;

        // prependLengthFieldType: 前置一个 u8 类型的字段表示当前字段的长度
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "用户名")
        private String name;

        @Preset.RustStyle.u16(desc = "年龄(16位无符号数)")
        private Integer age;

        // prependLengthFieldType: 前置一个 u8 类型的字段表示当前字段的长度
        @Preset.RustStyle.str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "地址")
        private String address;

    }
}
