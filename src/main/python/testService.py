import time

import grpc
import logging
from concurrent import futures
import threading
from proto import coordinate_pb2, coordinate_pb2_grpc

_ONE_DAY_IN_SECONDS = 60 * 60 * 24

# dummy coordinate class
class Coordinate:
    def __init__(self, x=0, y=0, z=0):
        self.x = x
        self.y = y
        self.z = z


# implementation of response
class CubeFinder(coordinate_pb2_grpc.CubeFinderServicer):
    def getCoordinates(self, request, context):
        return coordinate_pb2.CoorResponse(x=1, y=1, z=1)


# server start
def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    coordinate_pb2_grpc.add_CubeFinderServicer_to_server(CubeFinder(), server)

    print("Starting server, listening on port 50051...")
    server.add_insecure_port('[::]:50051')
    server.start()
    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    logging.basicConfig()
    serve()