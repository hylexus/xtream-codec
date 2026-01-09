-- drop table jt_808_request_trace_log;
-- drop table jt_808_response_trace_log;
-- drop table jt_808_alarm_attachment_info;
create table jt_808_request_trace_log
(
    id                    varchar(36) primary key,
    request_id            varchar(64),
    received_at           timestamp,
    net_type              integer,
    trace_id              varchar(36),
    terminal_id           varchar(36),
    message_id            integer,
    version               integer,
    flow_id               integer,
    message_body_length   integer,
    message_body_property integer,
    total_package         integer,
    current_package_no    integer,
    is_subpackage         boolean,
    raw_hex               text,
    escaped_hex           text,
    created_at            timestamp
);
alter table jt_808_request_trace_log
    owner to jt;
comment on column jt_808_request_trace_log.id is 'uuid';
comment on column jt_808_request_trace_log.request_id is 'RequestId';
comment on column jt_808_request_trace_log.received_at is '收到报文的时间';
comment on column jt_808_request_trace_log.net_type is '网络类型; 0:TCP, 1:UDP';
comment on column jt_808_request_trace_log.trace_id is '请求ID';
comment on column jt_808_request_trace_log.terminal_id is '终端手机号';
comment on column jt_808_request_trace_log.message_id is '消息ID';
comment on column jt_808_request_trace_log.version is '协议版本; 2019:2019, 2013:2013, 2011:2011, -2:unknown';
comment on column jt_808_request_trace_log.flow_id is '流水号';
comment on column jt_808_request_trace_log.message_body_length is '消息体长度';
comment on column jt_808_request_trace_log.message_body_property is '消息体属性';
comment on column jt_808_request_trace_log.total_package is '总包数';
comment on column jt_808_request_trace_log.current_package_no is '当前包序号';
comment on column jt_808_request_trace_log.is_subpackage is '是否是子包';
comment on column jt_808_request_trace_log.raw_hex is '原始报文';
comment on column jt_808_request_trace_log.escaped_hex is '转义之后的报文';
comment on column jt_808_request_trace_log.created_at is '数据插入时间';

create table jt_808_response_trace_log
(
    id          varchar(36) primary key,
    request_id  varchar(64) null,
    sent_at     timestamp,
    net_type    integer,
    trace_id    varchar(36),
    terminal_id varchar(36),
    escaped_hex text,
    created_at  timestamp
);
alter table jt_808_response_trace_log
    owner to jt;
comment on column jt_808_response_trace_log.id is 'uuid';
comment on column jt_808_response_trace_log.request_id is 'RequestId';
comment on column jt_808_response_trace_log.sent_at is '报文发送时间';
comment on column jt_808_response_trace_log.net_type is '网络类型; 0:TCP, 1:UDP';
comment on column jt_808_response_trace_log.trace_id is '请求ID';
comment on column jt_808_response_trace_log.terminal_id is '终端手机号';
comment on column jt_808_response_trace_log.escaped_hex is '转义之后的报文';
comment on column jt_808_response_trace_log.created_at is '数据插入时间';


create table jt_808_alarm_attachment_info
(
    id               varchar(36) primary key,
    terminal_id      varchar(36),
    alarm_no         varchar(64),
    alarm_time       timestamp,
    attachment_count int,
    file_name        varchar(256),
    file_type        int,
    file_size        bigint,
    file_path        varchar(256),
    alarm_sequence   int,
    client_id        varchar(64),
    created_at       timestamp
);
alter table jt_808_alarm_attachment_info
    owner to jt;
comment on column jt_808_alarm_attachment_info.id is 'uuid';
comment on column jt_808_alarm_attachment_info.terminal_id is '终端手机号';
comment on column jt_808_alarm_attachment_info.alarm_no is '平台分配的报警唯一编号';
comment on column jt_808_alarm_attachment_info.alarm_time is '报警时间';
comment on column jt_808_alarm_attachment_info.attachment_count is '报警附件数量';
comment on column jt_808_alarm_attachment_info.file_name is '文件名称';
comment on column jt_808_alarm_attachment_info.file_type is '文件类型 PICTURE=0,AUDIO=1,VIDEO=2,TEXT=3,OTHERS=4';
comment on column jt_808_alarm_attachment_info.file_size is '文件大小';
comment on column jt_808_alarm_attachment_info.file_path is '文件路径';
comment on column jt_808_alarm_attachment_info.alarm_sequence is '同一时间点报警的序号，从0循环累加';
comment on column jt_808_alarm_attachment_info.client_id is '终端 ID 7个字节，由大写字母和数字组成';
comment on column jt_808_alarm_attachment_info.created_at is '数据插入时间';
