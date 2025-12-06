<script setup lang="ts">
import {computed, reactive} from "vue";
import {DataField, DicKeyType, ValueLengthType} from "@/types/data-fields.ts";
import {ProtocolEditor} from "@/components/fields";
import {EncodeResult} from "@/types/model.ts";
import {requestEncodeMessageApi} from "@/api/codec-api.ts";
import {toHexString} from "@/utils/codec-utils.ts";

const bodyFields: DataField[] = [
  {type: 'u32', name: 'alarmFlag', value: 123, description: '报警标志',},
  {type: 'u32', name: 'status', value: 111, description: '状态',},
  {type: 'u32', name: 'latitude', value: 1234567, description: '纬度',},
  {type: 'u32', name: 'longitude', value: 123456789, description: '经度'},
  {type: 'u16', name: 'altitude', value: 123, description: '高度',},
  {type: 'u16', name: 'speed', value: 123, description: '速度'},
  {type: 'u16', name: 'direction', value: 123, description: '方向'},
  {type: 'bcd_8421_string', name: 'time', value: '251206131533', description: '时间',},
  {
    type: 'dict',
    name: 'extraItems',
    keyType: 'u8' as DicKeyType,
    valueLengthType: 'u8' as ValueLengthType,
    value: {
      0x01: {type: 'u32', name: '里程', value: 111},
      0x02: {type: 'u16', name: '油量', value: 222},
      0x03: {type: 'u16', name: '行驶记录功能获取的速度', value: 333},
      0x04: {type: 'u16', name: '需要人工确认报警事件的 ID', value: 444},
      0x05: {type: 'byte_seq', name: '胎压', value: [1, 2, 3]},
      0x06: {type: 'i16', name: '车厢温度', value: 26},
      0x11: {
        type: 'struct',
        name: '超速报警附加信息见 表 28',
        value: [
          {type: "u8", name: "位置类型", value: 1},
          {type: "u32", name: "区域或路段ID", value: 3},
        ] as DataField[]
      },
      0x25: {type: 'u32', name: '扩展车辆信号状态位', value: 555},
      0x2A: {type: 'u16', name: 'IO 状态位', value: 666},
      0x2B: {type: 'i32', name: '模拟量', value: 777},
      0x30: {type: 'u8', name: '无线通信网络信号强度', value: 100},
      0x31: {type: 'u8', name: 'GNSS定位卫星数', value: 101},
    } as Record<number, DataField>
  }
]

enum EncodeMode {
  SINGLE, MULTIPLE
}

interface PageState {
  encodeMode?: EncodeMode;
  query: {
    terminalId: string,
    version: string,
    encryptionType: number;
    bodyClass: string;
    messageId: number;
    flowId: number;
    reversedBit15InHeader: number;
    maxPackageSize: number;
    bodyData: DataField[];
  };
  encodeResult: Array<EncodeResult> | [];
}

const pageState = reactive<PageState>(<PageState>{
  encodeMode: undefined,
  query: {
    bodyClass: "dataField",
    terminalId: "00000000013912344329",
    version: "VERSION_2019",
    flowId: 0,
    encryptionType: 0,
    messageId: 0x0200,
    maxPackageSize: 1024,
    reversedBit15InHeader: 0,
    bodyData: bodyFields
  },
  encodeResult: []
})

const multiPackageHexString = computed(() => {
  if (!pageState || !pageState.encodeResult || pageState.encodeResult.length <= 0) {
    return undefined
  }
  return pageState.encodeResult.map(it => it.escapedHexString).join('\n')
})
const onEncodeBtbClick = async () => {
  console.log(pageState.query)
  const resp: Array<EncodeResult> = await requestEncodeMessageApi(pageState.query)

  pageState.encodeMode = resp.length > 1 ? EncodeMode.MULTIPLE : EncodeMode.SINGLE
  pageState.encodeResult = resp
  console.log(resp)
}

const defaultProps = {
  children: 'children',
  label: 'spanType',
}

</script>

<template>
  <div
      style="height: calc(100vh - 70px);"
  >
    <el-splitter>
      <el-splitter-panel collapsible :min="500">
        <div class="left-splitter">
          <el-form size="small"
                   label-width="auto" label-position="left"
                   style="display: flex; flex-wrap: wrap; align-items: center; justify-content: start;"
          >
            <el-form-item class="mr-5" label-width="50px" label="消息ID">
              <el-input-number v-model="pageState.query.messageId" :controls="false"
                               style="min-width: 150px;">
                <template #suffix>
                  <el-tag type="danger">{{ toHexString(pageState.query.messageId) }}</el-tag>
                </template>
              </el-input-number>
            </el-form-item>
            <el-form-item class="mr-5" label-width="50px" label="流水号">
              <el-input-number v-model="pageState.query.flowId"/>
            </el-form-item>
            <el-form-item class="mr-5" label-width="120px" label="保留位(消息头bit15)">
              <el-radio-group v-model="pageState.query.reversedBit15InHeader">
                <el-radio-button :value="0" label="0"/>
                <el-radio-button :value="1" label="1"/>
              </el-radio-group>
            </el-form-item>
            <el-form-item class="mr-5" label-width="60px" label="加密类型">
              <el-input-number v-model="pageState.query.encryptionType" disabled/>
            </el-form-item>
            <el-form-item class="mr-5" label-width="100px" label="单包最大字节数">
              <el-input-number v-model="pageState.query.maxPackageSize"/>
            </el-form-item>
            <el-form-item class="mr-5" label-width="40px" label="版本">
              <el-radio-group v-model="pageState.query.version">
                <el-radio-button label="2019" value="VERSION_2019"/>
                <el-radio-button label="2013" value="VERSION_2013"/>
              </el-radio-group>
            </el-form-item>
            <el-form-item class="mr-5" label-width="80px" label="终端手机号">
              <el-input v-model="pageState.query.terminalId"/>
            </el-form-item>
          </el-form>
          <ProtocolEditor
              v-model="pageState.query.bodyData"
              show-title
              title="消息体"
              style="width: 100%;"
          />
          <el-form-item>
            <el-button type="primary" @click="onEncodeBtbClick" style="width: 100%;" size="large">编码</el-button>
          </el-form-item>
        </div>
      </el-splitter-panel>
      <el-splitter-panel collapsible :min="100">
        <div class="right-splitter">
          <div v-if="pageState.encodeMode !== undefined">
            <div v-if="multiPackageHexString && pageState.encodeResult.length > 1" class="multi-package-hex-string">
              <el-alert type="success" :title="`当前报文编码为 ${pageState.encodeResult.length} 条分包报文`"
                        style="width: 100%; display: block;">
                <template #default>
                  <el-input v-model="multiPackageHexString" type="textarea" autosize readonly/>
                  分包详情如下：
                </template>
              </el-alert>
            </div>
            <el-card v-for="(item,index) in pageState.encodeResult" :key="index"
            >
              <template #header>
                #{{ index + 1 }}/{{ pageState.encodeResult.length }}
              </template>
              <div>
                <el-collapse :model-value="['1','2','3','4','5']">
                  <el-collapse-item name="1">
                    <template #title>
                      转义前
                    </template>
                    <el-input v-model="item.rawHexString" type="textarea" readonly autosize/>
                  </el-collapse-item>
                  <el-collapse-item name="2">
                    <template #title>
                      转义后&nbsp;<el-tag
                        v-if="item.rawHexString === item.escapedHexString"
                        type="success">无转义字符
                    </el-tag>
                      <el-tag v-else type="danger">有转义字符</el-tag>
                    </template>
                    <el-input v-model="item.escapedHexString" type="textarea" readonly autosize/>
                  </el-collapse-item>
                  <el-collapse-item title="报文结构" name="3">
                    <el-tree
                        style="width: 100%;overflow-x: scroll;"
                        :data="[item.details]"
                        node-key="id"
                        default-expand-all
                        :expand-on-click-node="false"
                        :props="defaultProps"
                    >
                      <template #default="{ data }">
                        <span class="custom-tree-node">
                          <el-tag type="info">{{ data.fieldName || data.spanType }}</el-tag>
                          <span v-if="data.spanType === 'RootSpan'">
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                            <el-tag type="success">实体类: {{ data.entityClass }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType === 'VirtualEntitySpan' ">
                            <el-tag type="danger" v-if="data.fieldDesc">{{ data.fieldDesc }}</el-tag>
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                            <el-tag type="success">实体类: {{ data.entityClass }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType === 'VirtualFieldSpan' ">
                            <el-tag type="danger" v-if="data.fieldDesc">{{ data.fieldDesc }}</el-tag>
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                            <el-tag type="warning">原始值: {{ data.value }}</el-tag>
                            <el-tag type="success">类型: {{ data.fieldType }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType === 'NestedFieldSpan'">
                            <el-tag type="danger" v-if="data.fieldDesc">{{ data.fieldDesc }}</el-tag>
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                            <el-tag type="success">类型: {{ data.fieldType }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType == 'BasicFieldSpan'">
                            <el-tag type="danger" v-if="data.fieldDesc">{{ data.fieldDesc }}</el-tag>
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                            <el-tag type="warning">原始值: {{ data.value }}</el-tag>
                            <el-tag type="success">解码器: {{ data.fieldCodec }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType == 'PrependLengthFieldSpan'">
                            <el-tag type="danger" v-if="data.fieldDesc">{{ data.fieldDesc }}</el-tag>
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                            <el-tag type="warning">原始值: {{ data.value }}</el-tag>
                            <el-tag type="success">解码器: {{ data.fieldCodec }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType === 'CollectionFieldSpan'">
                            <el-tag type="danger" v-if="data.fieldDesc">{{ data.fieldDesc }}</el-tag>
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                            <el-tag type="warning">类型: {{ data.fieldType }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType === 'CollectionItemSpan'">
                            <el-tag type="warning">offset: {{ data.offset }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType === 'MapFieldSpan'">
                            <el-tag type="danger" v-if="data.fieldDesc">{{ data.fieldDesc }}</el-tag>
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType === 'MapEntrySpan'">
                            <el-tag type="warning">offset: {{ data.offset }}</el-tag>
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                          </span>
                          <span v-else-if="data.spanType === 'MapEntryItemSpan'">
                            <el-tag type="primary">类型: {{ data.type }}</el-tag>
                            <el-tag type="primary">编码结果(HEX): {{ data.hexString }}</el-tag>
                            <el-tag type="warning">原始值: {{ data.value }}</el-tag>
                            <el-tag type="success">解码器: {{ data.fieldCodec }}</el-tag>
                          </span>
                        </span>
                      </template>
                    </el-tree>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </el-card>
          </div>
        </div>
      </el-splitter-panel>
    </el-splitter>
  </div>
</template>

<style scoped lang="scss">

</style>
