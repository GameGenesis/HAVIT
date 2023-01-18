import { Typography } from "@material-tailwind/react";

export default function Home() {
    return (
        <div className="home">
            <Typography
                as="a"
                href="#"
                variant="h1"
                className="mr-4 cursor-pointer py-1.5 font-normal">
                    WELCOME BACK.
            </Typography>

            <p>Home page</p>
        </div>
    );
}