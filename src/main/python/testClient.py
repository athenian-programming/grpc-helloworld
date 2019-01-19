import grpc
import logging

from proto import coordinate_pb2, coordinate_pb2_grpc


def run():
    # NOTE(gRPC Python Team): .close() is possible on a channel and should be
    # used in circumstances in which the with statement does not fit the needs
    # of the code.
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = coordinate_pb2_grpc.CubeFinderStub(channel)
        response = stub.getCoordinates(coordinate_pb2.CoorRequest(time="3.5 seconds"))


    print("Coordinates received: x={0} y={1} z={2}".format(response.x, response.y, response.z))


if __name__ == '__main__':
    logging.basicConfig()
    run()
