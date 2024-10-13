/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flinkClassifiersTesting;

import flinkClassifiersTesting.processors.factory.FlinkProcessFactory;
import flinkClassifiersTesting.processors.factory.dwm.DwmProcessFactory;
import flinkClassifiersTesting.processors.factory.dwm.DwmClassifierParams;
import flinkClassifiersTesting.processors.factory.vfdt.VfdtProcessFactory;
import flinkClassifiersTesting.processors.factory.vfdt.VfdtClassifierParams;
import flinkClassifiersTesting.processors.factory.vfdt.WindowedDetectorVfdtClassifierParams;

import java.util.List;

public class DataStreamJob {
    public static void main(String[] args) throws Exception {
        String dataset = "elec";
        String datasetPath = "/home/deikare/wut/streaming-datasets-formatted/" + dataset + ".csv";
        long bootstrapSamplesLimit = 100L;

        List<VfdtClassifierParams> vfdtParams = List.of(new VfdtClassifierParams(0.2, 0.1, 50));
        FlinkProcessFactory.runJobs(datasetPath, bootstrapSamplesLimit, VfdtProcessFactory.vfdt(vfdtParams));
        FlinkProcessFactory.runJobs(datasetPath, bootstrapSamplesLimit, VfdtProcessFactory.vfdtEntropy(vfdtParams));

        List<WindowedDetectorVfdtClassifierParams> wadVfdtParams = List.of(new WindowedDetectorVfdtClassifierParams(0.2, 0.1, 50, 1000, 0.9, 0.85, 1, 1));
        FlinkProcessFactory.runJobs(datasetPath, bootstrapSamplesLimit, VfdtProcessFactory.bstVfdtWindowedDetector(wadVfdtParams));

        List<DwmClassifierParams> dwmParams = List.of(new DwmClassifierParams(0.5, 0.2, 100));
        FlinkProcessFactory.runJobs(datasetPath, bootstrapSamplesLimit, DwmProcessFactory.extendedDwm(dwmParams));
        FlinkProcessFactory.runJobs(datasetPath, bootstrapSamplesLimit, DwmProcessFactory.dwm(dwmParams));
    }
}
