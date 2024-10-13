import os

if __name__ == "__main__":
    directory = "/home/deikare/wut/streaming-datasets"
    result_directory = directory + "-formatted"

    for filename in os.listdir(directory):
        full_filename = directory + "/" + filename

        if filename.endswith(".csv"):
            print(filename)

            different_classes = set()

            with open(full_filename) as file:
                full_result_filename = result_directory + "/" + filename

                with open(full_result_filename, "w") as result_file:
                    header = file.readline()

                    result_file.write(header)

                    for line in file:
                        parts = line.rstrip().split(',')
                        different_classes.add(parts[-1])
                        joinedParts = '#'.join(parts[:-1]) + ',' + parts[-1] + "\n"
                        result_file.write(joinedParts)

                filename_no_prefix = filename.rstrip().split(".csv")[0]
                class_number_filepath = result_directory + "/" + filename_no_prefix + ".txt"
                with open(class_number_filepath, "w") as class_number_file:
                    for idx, className in enumerate(different_classes):
                        class_number_file.write(className + " " + str(idx) + "\n")
