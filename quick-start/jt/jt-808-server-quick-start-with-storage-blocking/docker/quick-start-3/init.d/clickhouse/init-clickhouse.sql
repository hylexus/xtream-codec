create database jt_808;
use jt_808;

drop table if exists jt_808_request_trace_log;
create table jt_808_request_trace_log
(
    id                    UUID,
    request_id            String comment 'RequestId',
    received_at           DATETIME comment '收到报文的时间',
    net_type              Enum8('TCP' = 0, 'UDP' = 1) comment '网络类型',
    trace_id              String comment '请求ID',
    terminal_id           String comment '终端手机号',
    message_id            Int32 comment '消息ID',
    version               Enum16('unknown' = -2, 'v2011' = 2011, 'v2013' = 2013, 'v2019' = 2019) comment '协议版本',
    flow_id               Int32 comment '流水号',
    message_body_length   Int32 comment '消息体长度',
    message_body_property Int32 comment '消息体属性',
    total_package         Int32 comment '总包数',
    current_package_no    Int32 comment '当前包序号',
    is_subpackage         Bool comment '是否是子包',
    raw_hex               String comment '原始报文',
    escaped_hex           String comment '转义之后的报文',
    created_at            DATETIME comment '数据插入时间'
)
    engine = MergeTree PARTITION BY toYYYYMMDD(received_at)
        ORDER BY received_at
        TTL toDateTime(received_at) + toIntervalMonth(3)
        SETTINGS index_granularity = 8192;


drop table if exists jt_808_response_trace_log;
create table jt_808_response_trace_log
(
    id          UUID,
    request_id  Nullable(String) comment 'RequestId',
    sent_at     DATETIME comment '报文发送时间',
    net_type    Enum8('TCP' = 0, 'UDP' = 1) comment '网络类型',
    trace_id    String comment '请求ID',
    terminal_id String comment '终端手机号',
    escaped_hex String comment '转义之后的报文',
    created_at  DATETIME comment '数据插入时间'
)
    engine = MergeTree PARTITION BY toYYYYMMDD(sent_at)
        ORDER BY sent_at
        TTL toDateTime(sent_at) + toIntervalMonth(3)
        SETTINGS index_granularity = 8192;

drop table if exists jt_808_alarm_attachment_info;
create table jt_808_alarm_attachment_info
(
    id               UUID,
    terminal_id      String comment '终端手机号',
    alarm_no         String comment '平台分配的报警唯一编号',
    alarm_time       datetime comment '报警时间',
    attachment_count Int32 comment '报警附件数量',
    file_name        String comment '文件名称',
    file_type        Enum8('PICTURE' = 0, 'AUDIO' = 1, 'VIDEO'=2,'TEXT'=3,'OTHERS'=4) comment '文件类型',
    file_size        Int64 comment '文件大小',
    file_path        String comment '文件路径',
    alarm_sequence   Int32 comment '同一时间点报警的序号，从0循环累加',
    client_id        String comment '终端 ID 7个字节，由大写字母和数字组成',
    created_at       DATETIME comment '数据插入时间'
)
    engine = MergeTree PARTITION BY toYYYYMMDD(alarm_time)
        ORDER BY alarm_time
        TTL toDateTime(alarm_time) + toIntervalMonth(3)
        SETTINGS index_granularity = 8192;

