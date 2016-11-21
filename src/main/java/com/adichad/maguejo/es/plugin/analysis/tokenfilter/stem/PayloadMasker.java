package com.adichad.maguejo.es.plugin.analysis.tokenfilter.stem;

import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

/**
 * Created by adichad on 01/05/15.
 */
abstract class PayloadMasker {
    public abstract void mask();

    static final PayloadMasker noopMasker = new PayloadMasker() {
        public void mask(){}
    };

    static final class SingleBitMasker extends PayloadMasker {
        private final int payloadByteIndex;
        private final int payloadBitPosition;
        private final int payloadBytesMinSize;

        private PayloadAttribute payAtt;
        SingleBitMasker(int payloadBitPosition) {
            payloadByteIndex = payloadBitPosition >> 3;
            this.payloadBitPosition = (byte)(payloadBitPosition % 8);
            payloadBytesMinSize = payloadByteIndex + 1;
        }

        PayloadMasker init(PayloadAttribute payAtt) {
            this.payAtt = payAtt;
            return this;
        }

        @Override
        public void mask() {
            BytesRef payload = payAtt.getPayload();
            if (payload == null) {
                payload = new BytesRef(new byte[payloadBytesMinSize]);
            }
            byte[] b = payload.bytes;
            if (b.length < payloadBytesMinSize) {
                byte[] n = new byte[payloadBytesMinSize];
                System.arraycopy(payload.bytes, 0, n, 0, b.length);
                b = n;
            }

            b[payloadByteIndex] |= (1 << payloadBitPosition);
            payAtt.setPayload(payload);
        }

    }

}
