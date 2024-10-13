import argparse
import numpy as np
import os

from NormalDistributionGenerator import Generator, Config, get_name, plot, attr_1_mean_changes, \
    attr_1_larger_mean_changes, attr_1_std_dev_changes, attr_1_slow_mean_changes, attr_2_mean_changes, \
    attr_2_large_mean_changes, attr_2_slow_changes, attr_2_std_dev_changes, attr_3_mean_changes, \
    attr_3_large_mean_changes, attr_3_slow_changes, attr_3_std_dev_changes


def save_data(directory: str, attributes: np.ndarray, classes: list[int], config: Config, write_to_disk: bool = True):
    samples, attributes_num = attributes.shape
    n_classes = len(set(classes))

    name = get_name(attributes, classes, config)

    if write_to_disk:
        with open(f"{directory}/{name}.csv", "w") as f:
            headers = []
            for attributeIdx in range(attributes_num):
                headers.append(f"attribute{attributeIdx}")
            headers.append("class")

            f.write(",".join(headers))

            for sample in range(samples):
                f.write("\n")
                curr_attributes = []
                for attributeIdx in range(attributes_num):
                    curr_attributes.append(str(attributes[sample, attributeIdx]))

                f.write("#".join(curr_attributes))
                f.write(f",{classes[sample]}")

        with open(f"{directory}/{name}.txt", "w") as f:
            serializers = []
            for classIdx in range(n_classes):
                serializers.append(f"{classIdx} {classIdx}")
            f.write("\n".join(serializers))

    return name


if __name__ == '__main__':
    directory = "/home/deikare/wut/streaming-datasets-formatted"
    plots_directory = "/home/deikare/wut/mgr-flink/thesis/img/6/artificialDatasets"

    parser = argparse.ArgumentParser()
    parser.add_argument("--savePlots", action="store_true", help="save generated data plots with description",
                        required=False,
                        default=False)
    args = parser.parse_args()

    for config in [attr_1_mean_changes(), attr_1_larger_mean_changes(), attr_1_slow_mean_changes(),
                   attr_1_std_dev_changes(), attr_2_mean_changes(), attr_2_large_mean_changes(),
                   attr_2_slow_changes(), attr_2_std_dev_changes(), attr_3_mean_changes(),
                   attr_3_large_mean_changes(), attr_3_slow_changes(), attr_3_std_dev_changes()]:
        # for config in [attr_1_mean_changes()]:
        saveToFile = True

        print()

        subdirectory = f"{plots_directory}/{config.description}"
        if saveToFile and not os.path.exists(subdirectory):
            os.makedirs(subdirectory)
        generator = Generator(config)

        attributes, classes = generator.take(20000)

        plot(attributes, classes, config, saveToFile, plots_directory)
        name = save_data(directory, attributes, classes, config, write_to_disk=True)
        # print(f"{name} {config.description}")  # TODO capture it in bash script
        print(f"{name}")  # TODO capture it in bash script

    # TODO save figures as file and programmatically open them from bash script
