create database if not exists jt_808 default charset utf8mb4;
use jt_808;
drop table if exists jt_808_request_trace_log;
create table jt_808_request_trace_log
(
    id                    varchar(36) not null comment 'uuid'
        primary key,
    request_id            varchar(64) comment 'RequestId',
    received_at           datetime    null comment '收到报文的时间',
    net_type              int         null comment '网络类型; 0:TCP, 1:UDP',
    trace_id              varchar(36) null comment '请求ID',
    terminal_id           varchar(36) null comment '终端手机号',
    message_id            int         null comment '消息ID',
    version               int         null comment '协议版本; 20191:2019, 2013:2013, 2011:2011, -2:unknown',
    flow_id               int         null comment '流水号',
    message_body_length   int         null comment '消息体长度',
    message_body_property int         null comment '消息体属性',
    total_package         int         null comment '总包数',
    current_package_no    int         null comment '当前包序号',
    is_subpackage         tinyint(1)  null comment '是否是子包',
    raw_hex               text        null comment '原始报文',
    escaped_hex           text        null comment '转义之后的报文',
    created_at            datetime    null comment '数据插入时间'
);


drop table if exists jt_808_response_trace_log;
create table jt_808_response_trace_log
(
    id          varchar(36) not null comment 'uuid'
        primary key,
    request_id  varchar(64) null comment 'RequestId',
    sent_at     DATETIME comment '报文发送时间',
    net_type    int         null comment '网络类型; 0:TCP, 1:UDP',
    trace_id    varchar(36) null comment '请求ID',
    terminal_id varchar(36) null comment '终端手机号',
    escaped_hex text        null comment '转义之后的报文',
    created_at  datetime    null comment '数据插入时间'
);


drop table if exists jt_808_alarm_attachment_info;
create table jt_808_alarm_attachment_info
(
    id               varchar(36) comment 'uuid' primary key,
    terminal_id      varchar(36) comment '终端手机号',
    alarm_no         varchar(64) comment '平台分配的报警唯一编号',
    alarm_time       datetime comment '报警时间',
    attachment_count int comment '报警附件数量',
    file_name        varchar(256) comment '文件名称',
    file_type        int comment '文件类型 PICTURE=0,AUDIO=1,VIDEO=2,TEXT=3,OTHERS=4',
    file_size        bigint comment '文件大小',
    file_path        varchar(256) comment '文件路径',
    alarm_sequence   int comment '同一时间点报警的序号，从0循环累加',
    client_id        varchar(64) comment '终端 ID 7个字节，由大写字母和数字组成',
    created_at       DATETIME comment '数据插入时间'
);

